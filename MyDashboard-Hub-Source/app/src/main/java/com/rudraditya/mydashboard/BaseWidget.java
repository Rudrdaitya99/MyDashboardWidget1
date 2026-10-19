package com.rudraditya.mydashboard;
import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.net.Uri;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

abstract class BaseWidget extends AppWidgetProvider {
    abstract int layoutId();
    abstract String kind();
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) {
            RemoteViews v = new RemoteViews(c.getPackageName(), layoutId());
            SharedPreferences p = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            String date = new SimpleDateFormat("EEE · d MMM", Locale.getDefault()).format(new Date());
            int level = battery(c);
            String k = kind();
            if (k.equals("dashboard") || k.equals("clock")) {
                v.setTextViewText(R.id.timeText, time);
                v.setTextViewText(R.id.dateText, date);
            }
            if (k.equals("dashboard") || k.equals("battery")) v.setTextViewText(R.id.batteryText, "Battery · " + (level < 0 ? "--" : level + "%"));
            if (k.equals("dashboard") || k.equals("age")) v.setTextViewText(R.id.ageText, age(p.getString("birth", "")));
            if (k.equals("dashboard") || k.equals("countdown")) v.setTextViewText(R.id.countdownText, countdown(p.getString("target", "")));
            if (k.equals("dashboard")) v.setTextViewText(R.id.weatherText, "Weather · coming in a later update");
            if (k.equals("dashboard") || k.equals("ai")) {
                setLink(v, c, id, R.id.chatgptText, "https://chatgpt.com");
                setLink(v, c, id, R.id.geminiText, "https://gemini.google.com");
            }
            m.updateAppWidget(id, v);
        }
    }
    private int battery(Context c) {
        Intent i = c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return -1;
        int l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1), s = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        return l >= 0 && s > 0 ? Math.round(l * 100f / s) : -1;
    }
    private String age(String b) {
        if (b.isEmpty()) return "Age · set birthday in app";
        long n = days(b, new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        return n >= 0 ? "Age · " + n + " days" : "Check birthday date";
    }
    private String countdown(String t) {
        if (t.isEmpty()) return "Countdown · set target in app";
        long n = days(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()), t);
        return n > 0 ? "Countdown · " + n + " days left" : n == 0 ? "Countdown · today!" : "Countdown · date passed";
    }
    private long days(String a, String b) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            return TimeUnit.MILLISECONDS.toDays(f.parse(b).getTime() - f.parse(a).getTime());
        } catch (Exception e) { return -1; }
    }
    private void setLink(RemoteViews v, Context c, int id, int viewId, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PendingIntent p = PendingIntent.getActivity(c, id + viewId, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(viewId, p);
    }
}
