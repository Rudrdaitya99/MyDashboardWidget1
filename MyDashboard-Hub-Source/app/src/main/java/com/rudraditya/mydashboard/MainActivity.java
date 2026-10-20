package com.rudraditya.mydashboard;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.CompoundButton;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    static final String PREFS = "dashboard_settings";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        EditText birth = findViewById(R.id.birthInput);
        EditText target = findViewById(R.id.targetInput);
        TextView status = findViewById(R.id.statusText);
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        birth.setText(p.getString("birth", ""));
        target.setText(p.getString("target", ""));

        bindWidgetSwitch(R.id.enableDashboard, "dashboard", p);
        bindWidgetSwitch(R.id.enableAi, "ai", p);
        bindWidgetSwitch(R.id.enableClock, "clock", p);
        bindWidgetSwitch(R.id.enableBattery, "battery", p);
        bindWidgetSwitch(R.id.enableAge, "age", p);
        bindWidgetSwitch(R.id.enableCountdown, "countdown", p);

        findViewById(R.id.accentPurple).setOnClickListener(v -> setAccent(Color.rgb(189,178,255)));
        findViewById(R.id.accentBlue).setOnClickListener(v -> setAccent(Color.rgb(71,139,255)));
        findViewById(R.id.accentTeal).setOnClickListener(v -> setAccent(Color.rgb(38,183,165)));
        findViewById(R.id.accentPink).setOnClickListener(v -> setAccent(Color.rgb(227,106,174)));

        SeekBar opacity = findViewById(R.id.opacitySeek);
        TextView opacityLabel = findViewById(R.id.opacityLabel);
        int savedTransparency = p.contains("transparency") ? p.getInt("transparency", 20) : 100 - p.getInt("opacity", 85);
        opacity.setProgress(savedTransparency / 2);
        opacityLabel.setText("Glass transparency: " + savedTransparency + "%");
        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int value = progress * 2;
                opacityLabel.setText("Glass transparency: " + value + "%");
                if (fromUser) {
                    p.edit().putInt("transparency", value).putInt("opacity", 100 - value).apply();
                    DashboardRefresh.refresh(MainActivity.this);
                }
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> {
            String b = birth.getText().toString().trim();
            String t = target.getText().toString().trim();
            if (!validDate(b) || !validDate(t)) {
                status.setText("Use YYYY-MM-DD format, or leave a date blank.");
                return;
            }
            p.edit().putString("birth", b).putString("target", t).apply();
            status.setText("Saved. Widgets refreshed.");
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
            DashboardRefresh.refresh(this);
        });
        findViewById(R.id.chatgptButton).setOnClickListener(v -> openAppOrWeb("com.openai.chatgpt", "https://chatgpt.com"));
        findViewById(R.id.geminiButton).setOnClickListener(v -> openAppOrWeb("com.google.android.apps.bard", "https://gemini.google.com"));
    }

    private void bindWidgetSwitch(int id, String key, SharedPreferences p) {
        Switch sw = findViewById(id);
        sw.setChecked(p.getBoolean("enabled_" + key, true));
        sw.setOnCheckedChangeListener((button, checked) -> {
            p.edit().putBoolean("enabled_" + key, checked).apply();
            DashboardRefresh.refresh(this);
            Toast.makeText(this, checked ? "Widget enabled" : "Widget content paused", Toast.LENGTH_SHORT).show();
        });
    }

    private void setAccent(int color) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("accent", color).apply();
        DashboardRefresh.refresh(this);
        Toast.makeText(this, "Accent colour updated", Toast.LENGTH_SHORT).show();
    }

    private void openAppOrWeb(String packageName, String url) {
        Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
        if (i == null) i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try { startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "No app or browser available", Toast.LENGTH_SHORT).show(); }
    }

    private boolean validDate(String s) {
        if (s.isEmpty()) return true;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            return f.parse(s) != null && f.format(f.parse(s)).equals(s);
        } catch (Exception e) { return false; }
    }
}
