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
import android.graphics.Rect;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
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
    private int cardColor = Color.rgb(29,29,29);
    private int borderColor = Color.rgb(115,115,115);
    private Bitmap wallpaperPreview;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        buildScreen();
        ThemeAssets.prefetchWallpapers(this, () -> {
            if (!isFinishing()) {
                recreate();
                DashboardRefresh.refresh(this);
            }
        });
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(new WaveBackground());
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(28));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout hero = row();
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.app_icon);
        mark.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mark.setContentDescription("My Dashboard M mountain logo");
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
                new String[]{"Quick Panel", "Snow Ridge", "Stats Grid"});
        addGallery("Clock & Date", "clock", ClockWidget.class,
                new String[]{"Digital Glass", "Analog Dial", "Hybrid Snow Ridge"});
        addGallery("Battery", "battery", BatteryWidget.class,
                new String[]{"Minimal Battery", "Snow Ridge Battery", "Circular Meter"});
        addGallery("Age Counter", "age", AgeWidget.class,
                new String[]{"Days Count", "Snow Ridge Age", "Milestone Ring"});
        addGallery("Countdown", "countdown", CountdownWidget.class,
                new String[]{"Simple Countdown", "Snow Ridge Countdown", "Circular Timer"});
        addGallery("AI Folder", "ai", AIFolderWidget.class,
                new String[]{"Shortcut Pills", "Snow Ridge AI", "Icon Grid"});

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
        section.addView(text(title, 18, white, true));
        LinearLayout.LayoutParams subp = new LinearLayout.LayoutParams(-1, -2);
        subp.topMargin = dp(3);
        section.addView(text("THREE DISTINCT DESIGNS · PREVIEW BEFORE APPLY", 9, muted, true), subp);

        for (int style = 0; style < names.length; style++) {
            final int chosenStyle = style;
            final int[] chosenSize = {1}; // default preview is 3x1
            LinearLayout choice = column();
            choice.setPadding(dp(12), dp(12), dp(12), dp(12));
            choice.setBackground(shape(0xD91A1A1A, 0xAAFFFFFF, 18));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
            cp.topMargin = dp(10);
            section.addView(choice, cp);

            LinearLayout titleRow = row();
            titleRow.setGravity(Gravity.CENTER_VERTICAL);
            titleRow.addView(text(names[style], 14, white, true),
                    new LinearLayout.LayoutParams(0, -2, 1));
            TextView chip = text(style == 0 ? "GLASS" : style == 1 ? "PHOTO" : "ALT", 9, muted, true);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(shape(0xFF292929, 0x99FFFFFF, 20));
            titleRow.addView(chip, new LinearLayout.LayoutParams(dp(58), dp(25)));
            choice.addView(titleRow);

            LinearLayout sampleHolder = column();
            sampleHolder.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams sh = new LinearLayout.LayoutParams(-1, -2);
            sh.topMargin = dp(10);
            choice.addView(sampleHolder, sh);
            View sample = preview(key, chosenStyle, chosenSize[0]);
            sampleHolder.addView(sample, previewLayoutParams(key, chosenSize[0]));

            LinearLayout sizeRow = row();
            sizeRow.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams sizeRowParams = new LinearLayout.LayoutParams(-1, dp(34));
            sizeRowParams.topMargin = dp(8);
            choice.addView(sizeRow, sizeRowParams);
            TextView[] sizeChips = new TextView[3];
            String[] sizeLabels = {"2×1", "3×1", "3×2"};
            for (int size = 0; size < 3; size++) {
                final int selectedSize = size;
                TextView sizeChip = text(sizeLabels[size], 10, size == 1 ? Color.BLACK : white, true);
                sizeChip.setGravity(Gravity.CENTER);
                sizeChip.setBackground(shape(size == 1 ? Color.WHITE : 0xFF252525,
                        size == 1 ? Color.WHITE : 0x88FFFFFF, 12));
                LinearLayout.LayoutParams zp = new LinearLayout.LayoutParams(0, dp(30), 1);
                if (size > 0) zp.setMargins(dp(5), 0, 0, 0);
                sizeRow.addView(sizeChip, zp);
                sizeChips[size] = sizeChip;
                sizeChip.setOnClickListener(v -> {
                    chosenSize[0] = selectedSize;
                    for (int i = 0; i < sizeChips.length; i++) {
                        boolean active = i == selectedSize;
                        sizeChips[i].setTextColor(active ? Color.BLACK : white);
                        sizeChips[i].setBackground(shape(active ? Color.WHITE : 0xFF252525,
                                active ? Color.WHITE : 0x88FFFFFF, 12));
                    }
                    sampleHolder.removeAllViews();
                    View updated = preview(key, chosenStyle, chosenSize[0]);
                    sampleHolder.addView(updated, previewLayoutParams(key, chosenSize[0]));
                });
            }

            TextView use = action("APPLY DESIGN TO HOME SCREEN  ↗");
            LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(-1, dp(42));
            up.topMargin = dp(10);
            choice.addView(use, up);
            use.setOnClickListener(v -> applyDesign(key, chosenStyle, chosenSize[0],
                    provider, title, names[chosenStyle]));
        }
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(20);
        root.addView(section, sp);
    }

    private LinearLayout.LayoutParams previewLayoutParams(String key, int size) {
        int height = size == 0 ? dp(48) : size == 1 ? dp(68) : dp(126);
        int width = size == 0 ? dp(124) : -1;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        return lp;
    }

    private View preview(String key, int style, int size) {
        boolean large = size == 2;
        boolean compact = size == 0;
        int layout;
        if (key.equals("dashboard")) {
            if (style == 2) layout = compact ? R.layout.widget_dashboard_grid_compact : R.layout.widget_dashboard_grid;
            else if (style == 1) layout = large ? R.layout.widget_dashboard_snow : R.layout.widget_dashboard_compact_snow;
            else layout = large ? R.layout.widget_dashboard : R.layout.widget_dashboard_compact;
        } else if (key.equals("clock")) {
            if (style == 1) layout = compact ? R.layout.widget_clock_analog_compact : R.layout.widget_clock_analog;
            else if (style == 2) layout = compact ? R.layout.widget_clock_hybrid_compact : R.layout.widget_clock_hybrid;
            else layout = compact ? R.layout.widget_clock_compact : R.layout.widget_clock;
        } else if (key.equals("battery")) {
            if (style == 2) layout = compact ? R.layout.widget_battery_ring_compact : R.layout.widget_battery_ring;
            else if (style == 1) layout = compact ? R.layout.widget_battery_compact_snow : R.layout.widget_battery_snow;
            else layout = compact ? R.layout.widget_battery_compact : R.layout.widget_battery;
        } else if (key.equals("age")) {
            if (style == 2) layout = compact ? R.layout.widget_age_milestone_compact : R.layout.widget_age_milestone;
            else if (style == 1) layout = compact ? R.layout.widget_age_compact_snow : R.layout.widget_age_snow;
            else layout = compact ? R.layout.widget_age_compact : R.layout.widget_age;
        } else if (key.equals("countdown")) {
            if (style == 2) layout = compact ? R.layout.widget_countdown_circular_compact : R.layout.widget_countdown_circular;
            else if (style == 1) layout = compact ? R.layout.widget_countdown_compact_snow : R.layout.widget_countdown_snow;
            else layout = compact ? R.layout.widget_countdown_compact : R.layout.widget_countdown;
        } else {
            if (style == 2) layout = compact ? R.layout.widget_ai_grid_compact : R.layout.widget_ai_grid;
            else if (style == 1) layout = compact ? R.layout.widget_ai_compact : R.layout.widget_ai_snow;
            else layout = compact ? R.layout.widget_ai_compact : R.layout.widget_ai;
        }

        View actual = LayoutInflater.from(this).inflate(layout, null, false);
        String now = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("EEE · d MMM", Locale.getDefault()).format(new Date());
        setPreviewText(actual, R.id.timeText, now);
        setPreviewText(actual, R.id.dateText, date);
        setPreviewText(actual, R.id.batteryText, "Battery · 52%");
        setPreviewText(actual, R.id.batteryPercentText, "52%");
        setPreviewText(actual, R.id.batteryLabelText, "BATTERY");
        setPreviewText(actual, R.id.ageText, "Age · " + previewAge() + " days");
        setPreviewText(actual, R.id.ageNumberText, String.valueOf(previewAge()));
        setPreviewText(actual, R.id.ageLabelText, "DAYS ALIVE");
        setPreviewText(actual, R.id.countdownText, "Countdown · 42 days left");
        setPreviewText(actual, R.id.countdownNumberText, "42");
        setPreviewText(actual, R.id.countdownLabelText, "DAYS LEFT");
        setPreviewText(actual, R.id.weatherText, "Weather · 24°");
        setPreviewText(actual, R.id.chatgptText, "✳ ChatGPT");
        setPreviewText(actual, R.id.geminiText, "✦ Gemini");

        if ((style == 1 && !key.equals("clock")) || (key.equals("clock") && style == 2)) {
            ImageView mountain = actual.findViewById(R.id.mountainBackground);
            if (mountain != null) {
                Bitmap photo = ThemeAssets.widgetPhoto(this);
                if (photo != null) mountain.setImageBitmap(photo);
            }
            View card = actual.findViewById(R.id.widgetRoot);
            if (card != null) card.setBackgroundResource(ThemeAssets.widgetOverlay(this));
        }

        float textScale = size == 0 ? 0.82f : size == 1 ? 1.0f : 1.14f;
        scalePreviewText(actual, R.id.timeText, textScale);
        scalePreviewText(actual, R.id.dateText, textScale);
        scalePreviewText(actual, R.id.batteryText, textScale);
        scalePreviewText(actual, R.id.batteryPercentText, textScale);
        scalePreviewText(actual, R.id.batteryLabelText, textScale);
        scalePreviewText(actual, R.id.ageText, textScale);
        scalePreviewText(actual, R.id.ageNumberText, textScale);
        scalePreviewText(actual, R.id.ageLabelText, textScale);
        scalePreviewText(actual, R.id.countdownText, textScale);
        scalePreviewText(actual, R.id.countdownNumberText, textScale);
        scalePreviewText(actual, R.id.countdownLabelText, textScale);
        scalePreviewText(actual, R.id.chatgptText, textScale);
        scalePreviewText(actual, R.id.geminiText, textScale);
        return actual;
    }

    private void scalePreviewText(View parent, int id, float factor) {
        TextView view = parent.findViewById(id);
        if (view == null || factor <= 0f) return;
        view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                view.getTextSize() * factor);
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
        root.addView(text("WALLPAPER GALLERY", 11, muted, true));
        root.addView(text("Three distinct high-resolution mountain photographs", 13, white, false));
        LinearLayout p = panel();
        int selected = prefs.getInt("wallpaper_style", 0);
        ImageView image = new ImageView(this);
        wallpaperPreview = makeWallpaper(selected);
        image.setImageBitmap(wallpaperPreview);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(shape(Color.BLACK, Color.rgb(210,210,210), 16));
        image.setClipToOutline(true);
        LinearLayout.LayoutParams wallpaperSize = new LinearLayout.LayoutParams(dp(240), dp(440));
        wallpaperSize.gravity = Gravity.CENTER_HORIZONTAL;
        p.addView(image, wallpaperSize);

        String[] wallpaperNames = {"Snow Ridge", "Moonlit Summit", "Alpine Lake"};
        TextView[] choices = new TextView[wallpaperNames.length];
        LinearLayout row = row();
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(44));
        rowParams.topMargin = dp(10);
        p.addView(row, rowParams);
        for (int index = 0; index < wallpaperNames.length; index++) {
            final int chosen = index;
            TextView option = text(wallpaperNames[index], 10, index == selected ? Color.BLACK : white, true);
            option.setGravity(Gravity.CENTER);
            option.setBackground(shape(index == selected ? Color.WHITE : 0xFF242424,
                    index == selected ? Color.WHITE : 0x99FFFFFF, 12));
            LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(0, dp(42), 1);
            if (index > 0) optionParams.setMargins(dp(5), 0, 0, 0);
            row.addView(option, optionParams);
            choices[index] = option;
            option.setOnClickListener(v -> {
                prefs.edit().putInt("wallpaper_style", chosen).apply();
                wallpaperPreview = makeWallpaper(chosen);
                image.setImageBitmap(wallpaperPreview);
                for (int i = 0; i < choices.length; i++) {
                    boolean active = i == chosen;
                    choices[i].setTextColor(active ? Color.BLACK : white);
                    choices[i].setBackground(shape(active ? Color.WHITE : 0xFF242424,
                            active ? Color.WHITE : 0x99FFFFFF, 12));
                }
                DashboardRefresh.refresh(this);
            });
        }

        TextView apply = action("SET AS WALLPAPER");
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(46));
        ap.topMargin = dp(10);
        p.addView(apply, ap);
        root.addView(p);
        apply.setOnClickListener(v -> {
            try {
                WallpaperManager.getInstance(this).setBitmap(wallpaperPreview);
                Toast.makeText(this, "Wallpaper applied", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not set wallpaper. Check Android wallpaper permission.", Toast.LENGTH_LONG).show();
            }
        });
        TextView credit = text("Photographs are downloaded once and cached for offline reuse. Images: Unsplash.", 10, muted, false);
        LinearLayout.LayoutParams creditParams = new LinearLayout.LayoutParams(-1, -2);
        creditParams.topMargin = dp(6);
        p.addView(credit, creditParams);
    }

    private Bitmap makeWallpaper(int style) {
        return ThemeAssets.wallpaperPhoto(this, style);
    }

    private void applyDesign(String key, int style, int size, Class<?> provider, String title, String design) {
        prefs.edit().putInt("style_" + key, style).apply();
        DashboardRefresh.refresh(this);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        boolean accepted = false;
        if (manager.isRequestPinAppWidgetSupported()) {
            Bundle options = new Bundle();
            int minWidth = size == 0 ? 110 : 170;
            int maxWidth = size == 0 ? 130 : size == 1 ? 190 : 200;
            int minHeight = size == 2 ? 140 : size == 1 ? 70 : 40;
            int maxHeight = size == 2 ? 160 : size == 1 ? 85 : 50;
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWidth);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, maxWidth);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHeight);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, maxHeight);
            accepted = manager.requestPinAppWidget(new ComponentName(this, provider), options, null);
        }
        if (accepted) {
            Toast.makeText(this, design + " selected. Confirm Android's prompt to add " + title + ".", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, design + " saved; existing widgets refreshed. Your launcher may require its widget picker to add a new one.", Toast.LENGTH_LONG).show();
        }
    }

    private LinearLayout panel() {
        LinearLayout p = column();
        p.setPadding(dp(13), dp(12), dp(13), dp(12));
        p.setBackground(shape(Color.rgb(27,27,27), Color.rgb(100,100,100), 16));
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
        t.setBackground(shape(Color.rgb(34,34,34), Color.rgb(130,130,130), 13));
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
        String[] candidates = packageName.equals("com.google.android.apps.bard")
                ? new String[]{"com.google.android.apps.bard", "com.google.android.apps.gemini"}
                : new String[]{packageName};
        Intent launch = null;
        for (String candidate : candidates) {
            launch = getPackageManager().getLaunchIntentForPackage(candidate);
            if (launch != null) break;
        }
        if (launch == null) launch = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(launch);
        } catch (Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
            catch (Exception ignored) {
                Toast.makeText(this, "The app and browser are unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private static class WaveBackground extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float w = bounds.width(), h = bounds.height();
            canvas.save();
            canvas.translate(bounds.left, bounds.top);
            paint.setShader(new LinearGradient(0, 0, w * 0.85f, h, new int[]{0xFF191919, 0xFF101010, 0xFF242424, 0xFF0B0B0B}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < 10; i++) {
                paint.setStrokeWidth(i % 3 == 0 ? 1.5f : 0.8f);
                paint.setColor(Color.argb(i % 3 == 0 ? 75 : 28, 255, 255, 255));
                Path wave = new Path();
                float offset = h * (0.22f + i * 0.035f);
                wave.moveTo(-w * 0.1f, offset);
                wave.cubicTo(w * 0.20f, offset - h * 0.14f, w * 0.43f, offset + h * 0.12f, w * 0.66f, offset - h * 0.02f);
                wave.cubicTo(w * 0.82f, offset - h * 0.10f, w * 0.92f, offset + h * 0.12f, w * 1.1f, offset + h * 0.02f);
                canvas.drawPath(wave, paint);
            }
            paint.setStrokeWidth(1.4f);
            paint.setColor(0x44FFFFFF);
            canvas.drawCircle(w * 0.78f, h * 0.77f, Math.min(w, h) * 0.22f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.restore();
        }
        @Override public void setAlpha(int alpha) {}
        @Override public void setColorFilter(ColorFilter colorFilter) {}
        @Override public int getOpacity() { return PixelFormat.OPAQUE; }
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
