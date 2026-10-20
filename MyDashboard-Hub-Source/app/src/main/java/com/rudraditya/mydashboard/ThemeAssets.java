package com.rudraditya.mydashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Matrix;
import android.graphics.BitmapShader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Base64;
import android.util.Base64InputStream;
import java.io.IOException;
import java.io.InputStream;

/** Loads the bundled, photo-based monochrome mountain artwork used by Snow Ridge. */
final class ThemeAssets {
    private static Bitmap sourcePhoto;
    private static Bitmap widgetPhoto;
    private static Bitmap wallpaperPhoto;

    private ThemeAssets() {}

    private static synchronized Bitmap source(Context context) {
        if (sourcePhoto != null && !sourcePhoto.isRecycled()) return sourcePhoto;
        try (InputStream encoded = context.getAssets().open("mountain_widget.jpg.b64");
             InputStream decoded = new Base64InputStream(encoded, Base64.DEFAULT)) {
            sourcePhoto = BitmapFactory.decodeStream(decoded);
        } catch (IOException | RuntimeException e) {
            sourcePhoto = null;
        }
        return sourcePhoto;
    }

    static synchronized Bitmap widgetPhoto(Context context) {
        if (widgetPhoto != null && !widgetPhoto.isRecycled()) return widgetPhoto;
        Bitmap src = source(context);
        if (src == null) return null;

        int w = 480, h = 240;
        widgetPhoto = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(widgetPhoto);
        Path clip = new Path();
        clip.addRoundRect(new RectF(0, 0, w, h), 30, 30, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        Paint photo = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(src, null, new RectF(0, 0, w, h), photo);
        Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);
        shade.setColor(0x25000000);
        canvas.drawRect(0, 0, w, h, shade);
        canvas.restore();
        return widgetPhoto;
    }

    static synchronized Bitmap wallpaperPhoto(Context context) {
        if (wallpaperPhoto != null && !wallpaperPhoto.isRecycled()) return wallpaperPhoto;
        Bitmap src = source(context);
        if (src == null) return null;

        final int w = 720, h = 1600;
        wallpaperPhoto = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(wallpaperPhoto);
        Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setShader(new LinearGradient(0, 0, w, h,
                new int[]{0xFF030303, 0xFF202020, 0xFF080808, 0xFF020202},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, background);

        // Soft, enlarged central mountain silhouette fills the tall screen without drawing fake peaks.
        int cropWidth = Math.max(1, Math.min(src.getWidth(), Math.round(src.getHeight() * (w / (float) h))));
        int centerX = src.getWidth() / 2;
        int left = Math.max(0, Math.min(src.getWidth() - cropWidth, centerX - cropWidth / 2));
        Bitmap crop = Bitmap.createBitmap(src, left, 0, cropWidth, src.getHeight());
        Paint silhouette = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        silhouette.setAlpha(74);
        canvas.drawBitmap(crop, null, new RectF(0, 0, w, h), silhouette);
        if (crop != src && !crop.isRecycled()) crop.recycle();

        Paint topBottomShade = new Paint(Paint.ANTI_ALIAS_FLAG);
        topBottomShade.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{0xD9000000, 0x52000000, 0x18000000, 0x99000000, 0xF2000000},
                new float[]{0f, 0.28f, 0.48f, 0.77f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, topBottomShade);

        // The full real photograph remains sharp and recognizable in the middle of the wallpaper.
        float bandHeight = w / (src.getWidth() / (float) src.getHeight());
        float top = h * 0.39f;
        Paint sharpPhoto = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(src, null, new RectF(0, top, w, top + bandHeight), sharpPhoto);

        Paint fade = new Paint(Paint.ANTI_ALIAS_FLAG);
        fade.setShader(new LinearGradient(0, top - 36, 0, top + 64,
                new int[]{0xFF000000, 0x00000000}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, top - 36, w, top + 64, fade);
        fade.setShader(new LinearGradient(0, top + bandHeight - 58, 0, top + bandHeight + 55,
                new int[]{0x00000000, 0xE6000000}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, top + bandHeight - 58, w, top + bandHeight + 55, fade);
        return wallpaperPhoto;
    }
}
