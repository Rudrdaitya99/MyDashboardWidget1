package com.rudraditya.mydashboard;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

/** Free Open-Meteo weather lookup. Uses a city entered by the user; no location permission is needed. */
final class WeatherService {
    private static volatile boolean loading = false;
    private static final long CACHE_MS = 30L * 60L * 1000L;

    private WeatherService() {}

    static void refreshIfNeeded(Context context, boolean force) {
        Context app = context.getApplicationContext();
        SharedPreferences prefs = app.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        String city = prefs.getString("weather_city", "").trim();
        if (city.isEmpty()) {
            prefs.edit().putString("weather_summary", "Weather · set city in app")
                    .putString("weather_detail", "Add a city in My Dashboard to see local weather").apply();
            return;
        }
        long last = prefs.getLong("weather_fetched_at", 0L);
        if (!force && System.currentTimeMillis() - last < CACHE_MS
                && !prefs.getString("weather_summary", "").isEmpty()) return;
        synchronized (WeatherService.class) {
            if (loading) return;
            loading = true;
        }
        Thread worker = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
                JSONObject geo = getJson("https://geocoding-api.open-meteo.com/v1/search?name="
                        + encoded + "&count=1&language=en&format=json");
                JSONArray results = geo.optJSONArray("results");
                if (results == null || results.length() == 0) throw new Exception("City not found");
                JSONObject place = results.getJSONObject(0);
                double lat = place.getDouble("latitude");
                double lon = place.getDouble("longitude");
                String resolvedCity = place.optString("name", city);
                String region = place.optString("admin1", "");
                JSONObject forecast = getJson("https://api.open-meteo.com/v1/forecast?latitude="
                        + lat + "&longitude=" + lon
                        + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto");
                JSONObject current = forecast.getJSONObject("current");
                int temp = (int)Math.round(current.optDouble("temperature_2m", Double.NaN));
                int humidity = current.optInt("relative_humidity_2m", -1);
                int code = current.optInt("weather_code", -1);
                int wind = (int)Math.round(current.optDouble("wind_speed_10m", -1));
                String condition = condition(code);
                String shortPlace = resolvedCity;
                if (!region.isEmpty() && !region.equalsIgnoreCase(resolvedCity)) shortPlace += ", " + region;
                String summary = shortPlace + " · " + temp + "°C · " + condition;
                String detail = temp + "°C · " + condition
                        + (humidity >= 0 ? " · Humidity " + humidity + "%" : "")
                        + (wind >= 0 ? " · Wind " + wind + " km/h" : "");
                prefs.edit().putString("weather_summary", summary)
                        .putString("weather_detail", detail)
                        .putString("weather_resolved_city", shortPlace)
                        .putLong("weather_fetched_at", System.currentTimeMillis())
                        .putString("weather_error", "").apply();
            } catch (Exception e) {
                prefs.edit().putString("weather_summary", "Weather unavailable · check city/internet")
                        .putString("weather_detail", "Could not update weather. Check the city name and internet.")
                        .putString("weather_error", e.getClass().getSimpleName()).apply();
            } finally {
                if (connection != null) connection.disconnect();
                loading = false;
                DashboardRefresh.refresh(app);
            }
        }, "dashboard-weather-refresh");
        worker.setDaemon(true);
        worker.start();
    }

    private static JSONObject getJson(String address) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(9000);
            connection.setReadTimeout(9000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "MyDashboard-Android/3.4");
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) throw new Exception("HTTP " + status);
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }
            if (status < 200 || status >= 300) throw new Exception("HTTP " + status);
            return new JSONObject(body.toString());
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String condition(int code) {
        if (code == 0) return "Clear";
        if (code == 1) return "Mostly clear";
        if (code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code == 51 || code == 53 || code == 55) return "Drizzle";
        if (code == 56 || code == 57) return "Freezing drizzle";
        if (code == 61 || code == 63 || code == 65) return "Rain";
        if (code == 66 || code == 67) return "Freezing rain";
        if (code == 71 || code == 73 || code == 75 || code == 77) return "Snow";
        if (code == 80 || code == 81 || code == 82) return "Rain showers";
        if (code == 85 || code == 86) return "Snow showers";
        if (code == 95) return "Thunderstorm";
        if (code == 96 || code == 99) return "Thunderstorm with hail";
        return "Conditions unavailable";
    }
}
