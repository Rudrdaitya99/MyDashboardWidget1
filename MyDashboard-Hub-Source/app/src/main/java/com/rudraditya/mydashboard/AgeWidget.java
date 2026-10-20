package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
public class AgeWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_age; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        boolean mono = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_age", 0) == 1;
        int height = m.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70);
        if (height < 55) return mono ? R.layout.widget_age_compact_snow : R.layout.widget_age_compact;
        if (height < 110) return mono ? R.layout.widget_age_snow : R.layout.widget_age_compact;
        return mono ? R.layout.widget_age_snow : R.layout.widget_age;
    }
    @Override String kind() { return "age"; }
}
