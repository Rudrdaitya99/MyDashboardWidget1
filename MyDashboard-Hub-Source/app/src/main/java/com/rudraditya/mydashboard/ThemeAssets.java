package com.rudraditya.mydashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.util.Base64InputStream;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

final class ThemeAssets {
    private static final String[] FILES = {
        "snow_ridge.jpg", "moonlit_summit.jpg", "alpine_lake.jpg"
    };
    private static final String[] URLS = {
        "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=720&h=1600&q=86",
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=720&h=1600&q=86",
        "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=720&h=1600&q=86"
    };
    private static final Bitmap[] WALLPAPERS = new Bitmap[3];
    private static final Bitmap[] WIDGET_PHOTOS = new Bitmap[3];

    private ThemeAssets() {}

    static void prefetchWallpapers(Context context, Runnable onFinished) {
        Context app = context.getApplicationContext();
        Thread worker = new Thread(() -> {
            boolean changed = false;
            File dir = new File(app.getFilesDir(), "wallpapers");
            if (!dir.exists() && !dir.mkdirs()) return;
            for (int index = 0; index < URLS.length; index++) {
                File destination = new File(dir, FILES[index]);
                if (destination.exists() && destination.length() > 12000) continue;
                File temporary = new File(dir, FILES[index] + ".part");
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(URLS[index]).openConnection();
                    connection.setInstanceFollowRedirects(true);
                    connection.setConnectTimeout(7000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty("User-Agent", "MyDashboard-Android/3.3");
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) continue;
                    try (InputStream input = connection.getInputStream();
                         FileOutputStream output = new FileOutputStream(temporary)) {
                        byte[] buffer = new byte[8192];
                        int count;
                        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(temporary.getAbsolutePath(), options);
                    if (options.outWidth < 400 || options.outHeight < 700) {
                        temporary.delete();
                        continue;
                    }
                    if (destination.exists()) destination.delete();
                    if (temporary.renameTo(destination)) changed = true;
                } catch (Exception ignored) {
                    temporary.delete();
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
            if (changed) {
                synchronized (ThemeAssets.class) {
                    for (int i = 0; i < 3; i++) {
                        WALLPAPERS[i] = null;
                        WIDGET_PHOTOS[i] = null;
                    }
                }
                if (onFinished != null) new Handler(Looper.getMainLooper()).post(onFinished);
            }
        }, "dashboard-wallpaper-download");
        worker.setDaemon(true);
        worker.start();
    }

    static synchronized Bitmap wallpaperPhoto(Context context) {
        int selected = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
                .getInt("wallpaper_style", 0);
        return wallpaperPhoto(context, selected);
    }

    static synchronized Bitmap wallpaperPhoto(Context context, int style) {
        int index = Math.max(0, Math.min(2, style));
        if (WALLPAPERS[index] != null && !WALLPAPERS[index].isRecycled()) return WALLPAPERS[index];

        Bitmap source = readWallpaperFile(context, index);
        if (source == null) source = bundledFallback(context);
        if (source == null) return fallbackGradient();

        final int width = 720, height = 1600;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint photo = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        ColorMatrix gray = new ColorMatrix();
        gray.setSaturation(0f);
        photo.setColorFilter(new ColorMatrixColorFilter(gray));
        canvas.drawBitmap(source, null, new RectF(0, 0, width, height), photo);

        Paint contrast = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (index == 1) {
            contrast.setShader(new LinearGradient(0, 0, 0, height,
                    new int[]{0x65000000, 0x09000000, 0x6E000000}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, height, contrast);
        } else if (index == 2) {
            contrast.setShader(new LinearGradient(0, 0, width, height,
                    new int[]{0x24FFFFFF, 0x00000000, 0x32000000}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, height, contrast);
        } else {
            contrast.setShader(new LinearGradient(0, 0, 0, height,
                    new int[]{0x26000000, 0x00000000, 0x40000000}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, height, contrast);
        }

        if (source != null && source != WALLPAPERS[index] && source.getWidth() > 1000) {
            source.recycle();
        }
        WALLPAPERS[index] = result;
        return result;
    }

    static synchronized Bitmap widgetPhoto(Context context) {
        int selected = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
                .getInt("wallpaper_style", 0);
        return widgetPhoto(context, selected);
    }

    static synchronized Bitmap widgetPhoto(Context context, int style) {
        int index = Math.max(0, Math.min(2, style));
        if (WIDGET_PHOTOS[index] != null && !WIDGET_PHOTOS[index].isRecycled()) return WIDGET_PHOTOS[index];
        Bitmap wallpaper = wallpaperPhoto(context, index);
        if (wallpaper == null) return null;

        int width = 360, height = 180;
        int cropHeight = Math.min(wallpaper.getHeight(), wallpaper.getWidth() / 2);
        int centerY;
        if (index == 1) centerY = Math.round(wallpaper.getHeight() * 0.38f);
        else if (index == 2) centerY = Math.round(wallpaper.getHeight() * 0.64f);
        else centerY = Math.round(wallpaper.getHeight() * 0.48f);
        int top = Math.max(0, Math.min(wallpaper.getHeight() - cropHeight, centerY - cropHeight / 2));
        Bitmap crop = Bitmap.createBitmap(wallpaper, 0, top, wallpaper.getWidth(), cropHeight);
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(crop, null, new RectF(0, 0, width, height), paint);
        Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);
        shade.setColor(index == 1 ? 0x38000000 : 0x22000000);
        canvas.drawRect(0, 0, width, height, shade);
        crop.recycle();
        WIDGET_PHOTOS[index] = out;
        return out;
    }

    private static Bitmap readWallpaperFile(Context context, int index) {
        File file = new File(new File(context.getFilesDir(), "wallpapers"), FILES[index]);
        if (!file.exists() || file.length() < 12000) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    private static Bitmap bundledFallback(Context context) {
        String[] assets = {"mountain_wallpaper.jpg.b64", "mountain_widget.jpg.b64"};
        for (String asset : assets) {
            try (InputStream encoded = context.getAssets().open(asset);
                 InputStream decoded = new Base64InputStream(encoded, android.util.Base64.DEFAULT)) {
                Bitmap bitmap = BitmapFactory.decodeStream(decoded);
                if (bitmap != null) return bitmap;
            } catch (IOException | RuntimeException ignored) {}
        }
        return null;
    }

    private static Bitmap fallbackGradient() {
        Bitmap result = Bitmap.createBitmap(720, 1600, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, 0, 1600,
                new int[]{0xFF070707, 0xFF242424, 0xFF050505}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 720, 1600, paint);
        return result;
    }

    static int widgetOverlay(Context context) {
        int transparency = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
                .getInt("transparency", 20);
        if (transparency >= 70) return R.drawable.glass_mountain_overlay_80;
        if (transparency >= 50) return R.drawable.glass_mountain_overlay_60;
        if (transparency >= 30) return R.drawable.glass_mountain_overlay_40;
        if (transparency >= 10) return R.drawable.glass_mountain_overlay_20;
        return R.drawable.glass_mountain_overlay_0;
    }
}
