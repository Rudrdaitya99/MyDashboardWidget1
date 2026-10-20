package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ClockWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_clock; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        boolean mono = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_clock", 0) == 1;
        Bundle options = m.getAppWidgetOptions(id);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        if (mono) return minHeight < 110 ? R.layout.widget_clock_compact_mono : R.layout.widget_clock_mono;
        return minHeight < 110 ? R.layout.widget_clock_compact : R.layout.widget_clock;
    }
    @Override String kind() { return "clock"; }
}
