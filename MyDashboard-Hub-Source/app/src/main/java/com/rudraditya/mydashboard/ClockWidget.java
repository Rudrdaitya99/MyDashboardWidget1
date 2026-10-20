package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

public class ClockWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_clock; }

    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        int style = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_clock", 0);
        Bundle options = m.getAppWidgetOptions(id);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        boolean compact = minHeight < 110;
        if (style == 2) return compact ? R.layout.widget_clock_analog_compact : R.layout.widget_clock_analog;
        if (style == 1) return compact ? R.layout.widget_clock_compact_snow : R.layout.widget_clock_snow;
        return compact ? R.layout.widget_clock_compact : R.layout.widget_clock;
    }
    @Override String kind() { return "clock"; }
}
