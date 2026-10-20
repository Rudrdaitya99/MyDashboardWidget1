package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

public class DashboardWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_dashboard; }

    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        Bundle options = m.getAppWidgetOptions(id);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        return minHeight >= 110 ? R.layout.widget_dashboard : R.layout.widget_dashboard_compact;
    }

    @Override String kind() { return "dashboard"; }
}
