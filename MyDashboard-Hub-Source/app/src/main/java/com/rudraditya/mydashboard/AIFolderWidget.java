package com.rudraditya.mydashboard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

public class AIFolderWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_ai; }
    @Override int layoutId(Context c, AppWidgetManager m, int id) {
        boolean mono = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getInt("style_ai", 0) == 1;
        Bundle options = m.getAppWidgetOptions(id);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        if (mono) return R.layout.widget_ai_mono;
        return minHeight >= 110 ? R.layout.widget_ai : R.layout.widget_ai_compact;
    }
    @Override String kind() { return "ai"; }
}
