package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;

public class CountdownWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_countdown; }

    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        int style = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_countdown", 0);
        int height = m.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70);
        boolean compact = height < 110;
        if (style == 2) return compact ? R.layout.widget_countdown_circular_compact : R.layout.widget_countdown_circular;
        if (style == 1) return compact ? R.layout.widget_countdown_compact_snow : R.layout.widget_countdown_snow;
        return compact ? R.layout.widget_countdown_compact : R.layout.widget_countdown;
    }
    @Override String kind() { return "countdown"; }
}
