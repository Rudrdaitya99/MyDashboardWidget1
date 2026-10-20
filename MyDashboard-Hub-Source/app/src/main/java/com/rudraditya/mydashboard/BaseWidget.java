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
import android.os.Bundle;
import android.graphics.Color;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

abstract class BaseWidget extends AppWidgetProvider {
    abstract int layoutId();
    abstract String kind();

    int layoutId(Context c, AppWidgetManager m, int id) { return layoutId(); }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        SharedPreferences p = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        String k = kind();
        for (int id : ids) {
            if (!p.getBoolean("enabled_" + k, true)) {
                RemoteViews paused = new RemoteViews(c.getPackageName(), R.layout.widget_disabled);
                paused.setTextViewText(R.id.disabledTitle, k.toUpperCase(Locale.ROOT) + " PAUSED");
                m.updateAppWidget(id, paused);
                continue;
            }
            RemoteViews v = new RemoteViews(c.getPackageName(), layoutId(c, m, id));
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            String date = new SimpleDateFormat("EEE · d MMM", Locale.getDefault()).format(new Date());
            int level = battery(c);
            if (k.equals("dashboard") || k.equals("clock")) {
                v.setTextViewText(R.id.timeText, time);
                v.setTextViewText(R.id.dateText, date);
            }
            if (k.equals("dashboard") || k.equals("battery")) {
                v.setTextViewText(R.id.batteryText, "Battery · " + (level < 0 ? "--" : level + "%"));
            }
            if (k.equals("dashboard") || k.equals("age")) v.setTextViewText(R.id.ageText, age(p.getString("birth", "")));
            if (k.equals("dashboard") || k.equals("countdown")) v.setTextViewText(R.id.countdownText, countdown(p.getString("target", "")));
            if (k.equals("dashboard")) v.setTextViewText(R.id.weatherText, "Weather · coming in a later update");
            if (k.equals("dashboard") || k.equals("ai")) {
                setAppLink(v, c, id, R.id.chatgptText, "com.openai.chatgpt", "https://chatgpt.com");
                setAppLink(v, c, id, R.id.geminiText, "com.google.android.apps.bard", "https://gemini.google.com");
            }
            int accent = Color.WHITE;
            int transparency = p.getInt("transparency", 20);
            boolean whiteRidge = p.getInt("style_" + k, 0) == 1;
            if (whiteRidge) {
                try {
                    android.graphics.Bitmap mountain = ThemeAssets.widgetPhoto(c);
                    if (mountain != null) v.setImageViewBitmap(R.id.mountainBackground, mountain);
                    v.setInt(R.id.widgetRoot, "setBackgroundResource", ThemeAssets.widgetOverlay(c));
                } catch (Exception ignored) {}
            } else {
                int background = transparency >= 70 ? R.drawable.glass_card_80
                        : transparency >= 50 ? R.drawable.glass_card_60
                        : transparency >= 30 ? R.drawable.glass_card_40
                        : transparency >= 10 ? R.drawable.glass_card_20
                        : R.drawable.glass_card_0;
                try { v.setInt(R.id.widgetRoot, "setBackgroundResource", background); } catch (Exception ignored) {}
            }
            if (k.equals("dashboard") || k.equals("clock")) {
                try { v.setTextColor(R.id.timeText, accent); } catch (Exception ignored) {}
            }
            if (k.equals("dashboard") || k.equals("battery")) {
                try { v.setTextColor(R.id.batteryText, accent); } catch (Exception ignored) {}
            }
            if (k.equals("dashboard") || k.equals("age")) {
                try { v.setTextColor(R.id.ageText, accent); } catch (Exception ignored) {}
            }
            if (k.equals("dashboard") || k.equals("countdown")) {
                try { v.setTextColor(R.id.countdownText, accent); } catch (Exception ignored) {}
            }
            if (k.equals("dashboard") || k.equals("ai")) {
                try { v.setTextColor(R.id.chatgptText, accent); } catch (Exception ignored) {}
                try { v.setTextColor(R.id.geminiText, accent); } catch (Exception ignored) {}
            }
            applyResponsiveSizing(v, m.getAppWidgetOptions(id), k);
            m.updateAppWidget(id, v);
        }
    }

    @Override public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle options) {
        onUpdate(c, m, new int[] { id });
    }

    private void applyResponsiveSizing(RemoteViews v, Bundle options, String kind) {
        int width = options == null ? 170 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 170);
        int height = options == null ? 70 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70);
        float scale = Math.max(width / 170f, height / 70f);
        scale = Math.max(0.72f, Math.min(1.60f, scale));
        int[] ids = {R.id.timeText, R.id.dateText, R.id.batteryText, R.id.weatherText,
                R.id.ageText, R.id.countdownText, R.id.chatgptText, R.id.geminiText};
        float[] base = {30f, 12f, kind.equals("battery") ? 20f : 13f, 12f, kind.equals("age") ? 17f : 12f, kind.equals("countdown") ? 17f : 12f, 13f, 13f};
        for (int i = 0; i < ids.length; i++) {
            try { v.setTextViewTextSize(ids[i], android.util.TypedValue.COMPLEX_UNIT_SP, base[i] * scale); }
            catch (Exception ignored) {}
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
    private void setAppLink(RemoteViews v, Context c, int id, int viewId, String packageName, String url) {
        Intent i = c.getPackageManager().getLaunchIntentForPackage(packageName);
        if (i == null) {
            i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setPackage(packageName);
            if (i.resolveActivity(c.getPackageManager()) == null) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
        }
        PendingIntent p = PendingIntent.getActivity(c, id + viewId, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(viewId, p);
    }
}
