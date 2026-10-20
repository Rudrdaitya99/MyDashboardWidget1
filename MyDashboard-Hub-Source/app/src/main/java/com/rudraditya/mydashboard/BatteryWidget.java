package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
public class BatteryWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_battery; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        boolean mono = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_battery", 0) == 1;
        int height = m.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70);
        if (height < 110) return mono ? R.layout.widget_battery_mono : R.layout.widget_battery_compact;
        return mono ? R.layout.widget_battery_mono : R.layout.widget_battery;
    }
    @Override String kind() { return "battery"; }
}
