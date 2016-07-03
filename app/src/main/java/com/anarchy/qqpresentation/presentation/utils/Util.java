package com.anarchy.qqpresentation.presentation.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * <p/>
 * Date: 16/7/1 15:25
 * <p/>
 */
public class Util {
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;
    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 模糊图片
     * @param context
     * @param bitmap 需要模糊的原图片
     * @param radius
     * @return 模糊后的图片
     */
    public static Bitmap doBlur(Context context,Bitmap bitmap,int radius){
        Bitmap result =  Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap,0,0,null);
        RenderScript renderScript = RenderScript.create(context);
        Allocation allocation = Allocation.createFromBitmap(renderScript,result);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,allocation.getElement());
        blur.setInput(allocation);
        blur.setRadius(radius);
        blur.forEach(allocation);
        allocation.copyTo(result);
        return result;
    }

    /**
     * 设置view的背景
     * @param view
     * @param drawable
     */
    public static void setBackground(View view,Drawable drawable){
        if(Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        }else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
