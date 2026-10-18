package com.rudraditya.mydashboard;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    SharedPreferences prefs;
    EditText city, eventName, eventDate, shortcut1, shortcut2, shortcut3;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("dashboard", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1); root.setPadding(28, 28, 28, 28);
        root.setBackgroundColor(0xFF171B22);

        TextView title = label("MY DASHBOARD", 25, 0xFFC4FFE0);
        root.addView(title);
        TextView help = label("Configure the widget below. Your realme launcher stays unchanged.", 14, Color.WHITE);
        help.setPadding(0, 8, 0, 12); root.addView(help);

        city = field("Weather city (e.g. Beawar)", prefs.getString("city", "Beawar")); root.addView(city);
        eventName = field("Countdown title", prefs.getString("event_name", "My event")); root.addView(eventName);
        eventDate = field("Countdown date YYYY-MM-DD (optional)", prefs.getString("event_date", "")); root.addView(eventDate);
        shortcut1 = field("Shortcut 1 package name (optional)", prefs.getString("shortcut1", "")); root.addView(shortcut1);
        shortcut2 = field("Shortcut 2 package name (optional)", prefs.getString("shortcut2", "")); root.addView(shortcut2);
        shortcut3 = field("Shortcut 3 package name (optional)", prefs.getString("shortcut3", "")); root.addView(shortcut3);

        Button save = button("Save and refresh widget");
        save.setOnClickListener(v -> saveSettings()); root.addView(save);
        Button music = button("Enable music controls");
        music.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))); root.addView(music);
        Button refresh = button("Refresh now");
        refresh.setOnClickListener(v -> { DashboardWidget.refresh(this); Toast.makeText(this, "Refresh requested", 0).show(); }); root.addView(refresh);

        TextView note = label("Weather needs internet. Music controls need Notification access. Shortcut fields need Android package names (example: com.android.chrome).", 12, 0xFFB8C0CC);
        note.setPadding(0, 12, 0, 0); root.addView(note);
        ScrollView scroll = new ScrollView(this); scroll.addView(root); setContentView(scroll);
    }

    TextView label(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t;
    }
    EditText field(String hint, String value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setText(value);
        e.setTextColor(Color.WHITE); e.setHintTextColor(0xFF9299A6); return e;
    }
    Button button(String s) { Button b = new Button(this); b.setText(s); b.setAllCaps(false); return b; }

    void saveSettings() {
        String d = eventDate.getText().toString().trim();
        if (!d.isEmpty() && !d.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Date must use YYYY-MM-DD", Toast.LENGTH_LONG).show(); return;
        }
        prefs.edit().putString("city", city.getText().toString().trim())
            .putString("event_name", eventName.getText().toString().trim())
            .putString("event_date", d)
            .putString("shortcut1", shortcut1.getText().toString().trim())
            .putString("shortcut2", shortcut2.getText().toString().trim())
            .putString("shortcut3", shortcut3.getText().toString().trim()).apply();
        DashboardWidget.refresh(this);
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }
}
