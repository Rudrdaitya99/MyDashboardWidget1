package com.rudraditya.mydashboard;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.BatteryManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.RemoteViews;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardWidget extends AppWidgetProvider {
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) render(c, m, id);
        fetchWeather(c);
    }

    public static void refresh(Context c) {
        Intent i = new Intent(c, DashboardWidget.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, AppWidgetManager.getInstance(c)
            .getAppWidgetIds(new ComponentName(c, DashboardWidget.class)));
        c.sendBroadcast(i);
    }

    static void render(Context c, AppWidgetManager m, int id) {
        SharedPreferences p = c.getSharedPreferences("dashboard", Context.MODE_PRIVATE);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_dashboard);
        Intent bi = c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = bi == null ? -1 : bi.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = bi == null ? -1 : bi.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        v.setTextViewText(R.id.widget_battery, level >= 0 && scale > 0 ? "BATTERY  " + Math.round(level * 100f / scale) + "%" : "BATTERY  --");
        v.setTextViewText(R.id.widget_weather, p.getString("weather", "Weather · refresh to load"));
        String countdown = "No countdown set";
        String d = p.getString("event_date", "");
        String name = p.getString("event_name", "Event");
        if (!d.isEmpty()) try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(d));
            countdown = days > 0 ? name + " in " + days + " days" : days == 0 ? name + " is today!" : name + " was " + Math.abs(days) + " days ago";
        } catch (Exception ex) { countdown = "Check countdown date in app"; }
        v.setTextViewText(R.id.widget_countdown, countdown);

        v.setOnClickPendingIntent(R.id.button_settings, activity(c, Settings.ACTION_SETTINGS, 11));
        v.setOnClickPendingIntent(R.id.button_camera, activity(c, MediaStore.ACTION_IMAGE_CAPTURE, 12));
        v.setOnClickPendingIntent(R.id.button_music, musicApp(c));
        v.setOnClickPendingIntent(R.id.button_previous, media(c, "previous", 21));
        v.setOnClickPendingIntent(R.id.button_play_pause, media(c, "toggle", 22));
        v.setOnClickPendingIntent(R.id.button_next, media(c, "next", 23));
        shortcut(c, v, R.id.button_shortcut1, p.getString("shortcut1", ""), 31);
        shortcut(c, v, R.id.button_shortcut2, p.getString("shortcut2", ""), 32);
        shortcut(c, v, R.id.button_shortcut3, p.getString("shortcut3", ""), 33);
        m.updateAppWidget(id, v);
    }

    static PendingIntent activity(Context c, String action, int code) {
        Intent i = new Intent(action); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static PendingIntent musicApp(Context c) {
        Intent i = new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_APP_MUSIC);
        return PendingIntent.getActivity(c, 13, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static PendingIntent media(Context c, String command, int code) {
        Intent i = new Intent(c, DashboardNotificationListener.class);
        i.setAction(DashboardNotificationListener.ACTION_MEDIA); i.putExtra("command", command);
        return PendingIntent.getService(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static void shortcut(Context c, RemoteViews v, int view, String pkg, int code) {
        Intent launch = pkg.trim().isEmpty() ? null : c.getPackageManager().getLaunchIntentForPackage(pkg.trim());
        if (launch == null) {
            v.setTextViewText(view, pkg.trim().isEmpty() ? "Set shortcut" : "Not found");
            v.setOnClickPendingIntent(view, activity(c, Settings.ACTION_SETTINGS, code));
        } else {
            v.setTextViewText(view, pkg.substring(pkg.lastIndexOf('.') + 1));
            v.setOnClickPendingIntent(view, PendingIntent.getActivity(c, code, launch, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
    }

    static void fetchWeather(Context context) {
        Context c = context.getApplicationContext();
        String city = c.getSharedPreferences("dashboard", 0).getString("city", "Beawar");
        if (city == null || city.trim().isEmpty()) return;
        EXEC.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String q = URLEncoder.encode(city.trim(), "UTF-8");
                conn = (HttpURLConnection)new URL("https://geocoding-api.open-meteo.com/v1/search?name=" + q + "&count=1&language=en&format=json").openConnection();
                conn.setConnectTimeout(7000); conn.setReadTimeout(7000);
                JSONObject g = new JSONObject(read(conn));
                if (!g.has("results") || g.getJSONArray("results").length() == 0) throw new Exception("City not found");
                JSONObject place = g.getJSONArray("results").getJSONObject(0);
                double lat = place.getDouble("latitude"), lon = place.getDouble("longitude");
                conn.disconnect();
                conn = (HttpURLConnection)new URL("https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current=temperature_2m,relative_humidity_2m,apparent_temperature&timezone=auto").openConnection();
                conn.setConnectTimeout(7000); conn.setReadTimeout(7000);
                JSONObject current = new JSONObject(read(conn)).getJSONObject("current");
                String text = city + "  " + Math.round(current.getDouble("temperature_2m")) + "°C · feels " + Math.round(current.getDouble("apparent_temperature")) + "°C · RH " + current.getInt("relative_humidity_2m") + "%";
                c.getSharedPreferences("dashboard", 0).edit().putString("weather", text).apply();
            } catch (Exception ex) {
                c.getSharedPreferences("dashboard", 0).edit().putString("weather", "Weather unavailable · check city/internet").apply();
            } finally { if (conn != null) conn.disconnect(); }
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            for (int id : m.getAppWidgetIds(new ComponentName(c, DashboardWidget.class))) render(c, m, id);
        });
    }
    static String read(HttpURLConnection c) throws Exception {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
            StringBuilder b = new StringBuilder(); String line;
            while ((line = r.readLine()) != null) b.append(line);
            return b.toString();
        }
    }
}
