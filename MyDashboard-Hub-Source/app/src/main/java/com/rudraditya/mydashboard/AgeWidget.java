package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
public class AgeWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_age; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        return c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_age", 0) == 1 ? R.layout.widget_age_mono : R.layout.widget_age;
    }
    @Override String kind() { return "age"; }
}
