package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.content.SharedPreferences;

public class DashboardWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_dashboard; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        SharedPreferences p = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        boolean mono = p.getInt("style_dashboard", 0) == 1;
        Bundle options = m.getAppWidgetOptions(id);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        if (mono) return minHeight >= 110 ? R.layout.widget_dashboard_snow : R.layout.widget_dashboard_compact_snow;
        return minHeight >= 110 ? R.layout.widget_dashboard : R.layout.widget_dashboard_compact;
    }
    @Override String kind() { return "dashboard"; }
}
