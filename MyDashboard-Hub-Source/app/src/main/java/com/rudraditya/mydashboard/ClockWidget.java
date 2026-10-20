package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

public class ClockWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_clock; }

    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        Bundle options = m.getAppWidgetOptions(id);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        return minHeight < 110 ? R.layout.widget_clock_compact : R.layout.widget_clock;
    }

    @Override String kind() { return "clock"; }
}
