package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
public class BatteryWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_battery; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        return c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_battery", 0) == 1 ? R.layout.widget_battery_mono : R.layout.widget_battery;
    }
    @Override String kind() { return "battery"; }
}
