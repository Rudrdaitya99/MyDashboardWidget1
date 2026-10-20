package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

public class AIFolderWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_ai; }

    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        int style = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_ai", 0);
        int height = m.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70);
        boolean compact = height < 110;
        if (style == 2) return compact ? R.layout.widget_ai_grid_compact : R.layout.widget_ai_grid;
        if (style == 1) return compact ? R.layout.widget_ai_compact_snow : R.layout.widget_ai_snow;
        return compact ? R.layout.widget_ai_compact : R.layout.widget_ai;
    }
    @Override String kind() { return "ai"; }
}
