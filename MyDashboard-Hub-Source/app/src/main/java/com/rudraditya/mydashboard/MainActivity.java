package com.rudraditya.mydashboard;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.app.WallpaperManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    static final String PREFS = "dashboard_settings";
    private LinearLayout root;
    private SharedPreferences prefs;
    private int white = Color.WHITE;
    private int muted = Color.rgb(190,190,190);
    private int cardColor = Color.rgb(22,22,22);
    private int borderColor = Color.rgb(115,115,115);
    private Bitmap wallpaperPreview;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(10,10,10));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(28));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout hero = row();
        TextView mark = text("◈", 27, white, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(shape(Color.WHITE, 0xFF111111, 18));
        hero.addView(mark, new LinearLayout.LayoutParams(dp(54), dp(54)));
        LinearLayout heroText = column();
        heroText.setPadding(dp(12), 0, 0, 0);
        heroText.addView(text("My Dashboard", 25, white, true));
        heroText.addView(text("MONOCHROME WIDGET STUDIO", 10, muted, true));
        hero.addView(heroText, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(hero);
        space(root, 22);
        root.addView(text("CHOOSE A LOOK", 11, muted, true));
        root.addView(text("Tap a visual design to apply it.", 14, white, false));
        root.addView(text("Android may ask you to confirm adding it to your home screen.", 11, muted, false));
        space(root, 16);

        addGallery("Dashboard Hub", "dashboard", DashboardWidget.class,
                new String[]{"Essential", "Mono Glass"});
        addGallery("Clock & Date", "clock", ClockWidget.class,
                new String[]{"Classic", "Timeless"});
        addGallery("Battery", "battery", BatteryWidget.class,
                new String[]{"Simple", "Battery Bar"});
        addGallery("Age Counter", "age", AgeWidget.class,
                new String[]{"Counter", "Minimal"});
        addGallery("Countdown", "countdown", CountdownWidget.class,
                new String[]{"Counter", "Minimal"});
        addGallery("AI Folder", "ai", AIFolderWidget.class,
                new String[]{"List", "Split Glass"});

        addWallpaperSection();

        space(root, 10);
        root.addView(text("APPEARANCE", 11, muted, true));
        LinearLayout appearance = panel();
        appearance.addView(text("Glass transparency", 14, white, true));
        TextView opacityLabel = text("Background transparency: " + prefs.getInt("transparency", 20) + "%", 12, muted, false);
        appearance.addView(opacityLabel);
        SeekBar opacity = new SeekBar(this);
        opacity.setMax(80);
        opacity.setProgress(prefs.getInt("transparency", 20));
        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) {
                int val = Math.min(80, progress);
                opacityLabel.setText("Background transparency: " + val + "%");
                if (fromUser) {
                    prefs.edit().putInt("transparency", val).putInt("opacity", 100-val).apply();
                    DashboardRefresh.refresh(MainActivity.this);
                }
            }
            public void onStartTrackingTouch(SeekBar seek) {}
            public void onStopTrackingTouch(SeekBar seek) {}
        });
        appearance.addView(opacity);
        root.addView(appearance);

        space(root, 10);
        root.addView(text("PERSONAL COUNTERS", 11, muted, true));
        LinearLayout personal = panel();
        EditText birth = edit("Birthday · YYYY-MM-DD", prefs.getString("birth", ""));
        EditText target = edit("Countdown target · YYYY-MM-DD", prefs.getString("target", ""));
        personal.addView(birth);
        personal.addView(target);
        TextView save = action("SAVE COUNTERS");
        save.setOnClickListener(v -> {
            String b = birth.getText().toString().trim();
            String t = target.getText().toString().trim();
            if (!validDate(b) || !validDate(t)) {
                Toast.makeText(this, "Use YYYY-MM-DD, or leave a date blank", Toast.LENGTH_LONG).show();
                return;
            }
            prefs.edit().putString("birth", b).putString("target", t).apply();
            DashboardRefresh.refresh(this);
            Toast.makeText(this, "Counters saved", Toast.LENGTH_SHORT).show();
        });
        personal.addView(save);
        root.addView(personal);

        space(root, 10);
        root.addView(text("QUICK LAUNCH", 11, muted, true));
        LinearLayout apps = row();
        TextView chat = action("✳  ChatGPT");
        TextView gem = action("✦  Gemini");
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, dp(48), 1);
        half.setMargins(0, 0, dp(5), 0);
        apps.addView(chat, half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, dp(48), 1);
        half2.setMargins(dp(5), 0, 0, 0);
        apps.addView(gem, half2);
        chat.setOnClickListener(v -> openAppOrWeb("com.openai.chatgpt", "https://chatgpt.com"));
        gem.setOnClickListener(v -> openAppOrWeb("com.google.android.apps.bard", "https://gemini.google.com"));
        root.addView(apps);
    }

    private void addGallery(String title, String key, Class<?> provider, String[] names) {
        LinearLayout section = column();
        TextView heading = text(title, 17, white, true);
        section.addView(heading);
        TextView sub = text("TAP A DESIGN TO APPLY", 9, muted, true);
        section.addView(sub);
        LinearLayout choices = row();
        choices.setGravity(Gravity.TOP);
        for (int style = 0; style < 2; style++) {
            final int chosenStyle = style;
            LinearLayout choice = column();
            choice.setPadding(dp(9), dp(9), dp(9), dp(9));
            choice.setBackground(shape(cardColor, borderColor, 16));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1);
            cp.setMargins(style == 0 ? 0 : dp(7), dp(8), style == 0 ? dp(3) : 0, 0);
            choices.addView(choice, cp);
            choice.addView(preview(key, style));
            TextView label = text(names[style], 12, white, true);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = dp(8);
            choice.addView(label, lp);
            TextView use = text("USE DESIGN  ↗", 9, white, true);
            use.setGravity(Gravity.CENTER);
            use.setPadding(dp(4), dp(9), dp(4), dp(9));
            use.setBackground(shape(Color.rgb(29,29,29), Color.rgb(92,92,92), 10));
            LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(-1, -2);
            up.topMargin = dp(8);
            choice.addView(use, up);
            choice.setOnClickListener(v -> applyDesign(key, chosenStyle, provider, title, names[chosenStyle]));
        }
        section.addView(choices);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(17);
        root.addView(section, sp);
    }

    private View preview(String key, int style) {
        int layout;
        if (key.equals("dashboard")) layout = style == 0 ? R.layout.widget_dashboard_compact : R.layout.widget_dashboard_compact_mono;
        else if (key.equals("clock")) layout = style == 0 ? R.layout.widget_clock_compact : R.layout.widget_clock_compact_mono;
        else if (key.equals("battery")) layout = style == 0 ? R.layout.widget_battery : R.layout.widget_battery_mono;
        else if (key.equals("age")) layout = style == 0 ? R.layout.widget_age : R.layout.widget_age_mono;
        else if (key.equals("countdown")) layout = style == 0 ? R.layout.widget_countdown : R.layout.widget_countdown_mono;
        else layout = style == 0 ? R.layout.widget_ai_compact : R.layout.widget_ai_mono;

        View actual = LayoutInflater.from(this).inflate(layout, null, false);
        actual.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(key.equals("dashboard") ? 76 : 68)));
        String now = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("EEE · d MMM", Locale.getDefault()).format(new Date());
        setPreviewText(actual, R.id.timeText, now);
        setPreviewText(actual, R.id.dateText, date);
        setPreviewText(actual, R.id.batteryText, "Battery · 52%");
        setPreviewText(actual, R.id.ageText, "Age · " + previewAge() + " days");
        setPreviewText(actual, R.id.countdownText, "Countdown · 42 days left");
        setPreviewText(actual, R.id.weatherText, "Weather · 24°");
        return actual;
    }

    private void setPreviewText(View parent, int id, String value) {
        TextView t = parent.findViewById(id);
        if (t != null) t.setText(value);
    }

    private long previewAge() {
        String birth = prefs.getString("birth", "");
        if (birth.isEmpty()) return 5145;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            return Math.max(0, (new Date().getTime() - f.parse(birth).getTime()) / 86400000L);
        } catch (Exception e) { return 5145; }
    }

    private void addWallpaperSection() {
        root.addView(text("WALLPAPER", 11, muted, true));
        root.addView(text("Monochrome waves · matched to your glass widgets", 13, white, false));
        LinearLayout p = panel();
        ImageView image = new ImageView(this);
        wallpaperPreview = makeWallpaper();
        image.setImageBitmap(wallpaperPreview);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(shape(Color.BLACK, Color.rgb(180,180,180), 16));
        image.setClipToOutline(true);
        p.addView(image, new LinearLayout.LayoutParams(-1, dp(190)));
        TextView apply = action("SET AS WALLPAPER");
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(46));
        ap.topMargin = dp(10);
        p.addView(apply, ap);
        root.addView(p);
        apply.setOnClickListener(v -> {
            try {
                WallpaperManager.getInstance(this).setBitmap(wallpaperPreview);
                Toast.makeText(this, "Monochrome wallpaper applied", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not set wallpaper. Check Android wallpaper permission.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private Bitmap makeWallpaper() {
        int w = 720, h = 1440;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setShader(new LinearGradient(0, 0, w, h, new int[]{0xFF050505, 0xFF181818, 0xFF030303}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bg);
        for (int i = 0; i < 18; i++) {
            Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(i % 4 == 0 ? 2.2f : 1.0f);
            line.setColor(Color.argb(i % 4 == 0 ? 125 : 55, 238, 238, 238));
            Path path = new Path();
            float offset = i * 22f;
            path.moveTo(-60, h * 0.30f + offset);
            path.cubicTo(w * 0.22f, h * 0.22f + offset, w * 0.38f, h * 0.49f - offset * 0.15f, w * 0.63f, h * 0.37f + offset * 0.25f);
            path.cubicTo(w * 0.82f, h * 0.28f + offset * 0.1f, w * 0.86f, h * 0.55f + offset * 0.3f, w + 60, h * 0.47f + offset * 0.12f);
            canvas.drawPath(path, line);
        }
        Paint orb = new Paint(Paint.ANTI_ALIAS_FLAG);
        orb.setShader(new LinearGradient(w * 0.2f, h * 0.65f, w * 0.85f, h * 0.92f, 0xFF444444, 0xFF050505, Shader.TileMode.CLAMP));
        canvas.drawCircle(w * 0.52f, h * 0.80f, w * 0.31f, orb);
        Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(2f);
        edge.setColor(0x99FFFFFF);
        canvas.drawCircle(w * 0.52f, h * 0.80f, w * 0.31f, edge);
        return bitmap;
    }

    private void applyDesign(String key, int style, Class<?> provider, String title, String design) {
        prefs.edit().putInt("style_" + key, style).apply();
        DashboardRefresh.refresh(this);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        boolean accepted = false;
        if (manager.isRequestPinAppWidgetSupported()) {
            accepted = manager.requestPinAppWidget(new ComponentName(this, provider), null, null);
        }
        if (accepted) {
            Toast.makeText(this, design + " selected. Confirm the system prompt to add " + title + ".", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, design + " selected and existing widgets refreshed. To add a new one, use your launcher's widget picker.", Toast.LENGTH_LONG).show();
        }
    }

    private LinearLayout panel() {
        LinearLayout p = column();
        p.setPadding(dp(13), dp(12), dp(13), dp(12));
        p.setBackground(shape(Color.rgb(13,13,13), Color.rgb(49,49,49), 16));
        return p;
    }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }
    private TextView action(String label) {
        TextView t = text(label, 12, white, true); t.setGravity(Gravity.CENTER);
        t.setBackground(shape(Color.rgb(24,24,24), Color.rgb(100,100,100), 13));
        t.setPadding(dp(10), dp(10), dp(10), dp(10));
        return t;
    }
    private EditText edit(String hint, String value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setText(value); e.setHint(hint);
        e.setHintTextColor(muted); e.setTextColor(white); e.setTextSize(13);
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(135,135,135)));
        return e;
    }
    private GradientDrawable shape(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(fill); d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke); return d;
    }
    private void space(LinearLayout l, int height) {
        View v = new View(this); l.addView(v, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private void openAppOrWeb(String packageName, String url) {
        Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
        if (i == null) i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try { startActivity(i); } catch (Exception e) {
            Toast.makeText(this, "No app or browser available", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean validDate(String s) {
        if (s.isEmpty()) return true;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            Date d = f.parse(s);
            return d != null && f.format(d).equals(s);
        } catch (Exception e) { return false; }
    }
}
