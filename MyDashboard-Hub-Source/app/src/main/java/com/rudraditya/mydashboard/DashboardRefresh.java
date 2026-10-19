package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
final class DashboardRefresh {
    static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        Class<?>[] classes = {DashboardWidget.class, ClockWidget.class, BatteryWidget.class, AgeWidget.class, CountdownWidget.class, AIFolderWidget.class};
        for (Class<?> cls : classes) {
            int[] ids = m.getAppWidgetIds(new ComponentName(c, cls));
            if (ids.length > 0) {
                Intent i = new Intent(c, cls).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                c.sendBroadcast(i);
            }
        }
    }
}
