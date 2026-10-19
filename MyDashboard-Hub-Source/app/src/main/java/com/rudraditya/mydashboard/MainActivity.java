package com.rudraditya.mydashboard;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.EditText;
import android.widget.TextView;
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
        findViewById(R.id.saveButton).setOnClickListener(v -> {
            String b = birth.getText().toString().trim();
            String t = target.getText().toString().trim();
            if (!validDate(b) || !validDate(t)) {
                status.setText("Use YYYY-MM-DD format, or leave a date blank.");
                return;
            }
            p.edit().putString("birth", b).putString("target", t).apply();
            status.setText("Saved. Re-add widgets if they don't refresh.");
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
            DashboardRefresh.refresh(this);
        });
        findViewById(R.id.chatgptButton).setOnClickListener(v -> open("https://chatgpt.com"));
        findViewById(R.id.geminiButton).setOnClickListener(v -> open("https://gemini.google.com"));
    }
    private void open(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show(); }
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
