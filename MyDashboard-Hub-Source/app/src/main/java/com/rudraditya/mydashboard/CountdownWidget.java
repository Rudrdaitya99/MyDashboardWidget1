package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
public class CountdownWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_countdown; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        return c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_countdown", 0) == 1 ? R.layout.widget_countdown_mono : R.layout.widget_countdown;
    }
    @Override String kind() { return "countdown"; }
}
