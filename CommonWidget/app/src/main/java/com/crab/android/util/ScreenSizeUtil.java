package com.crab.android.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class ScreenSizeUtil {
    /**
     * 当前的最小屏幕宽度
     * @param context android context
     * @return 当前的最小屏幕宽度
     */
    public static int getCurrentSwDp(Context context) {
        final Resources res = context.getResources();
        final DisplayMetrics metrics = res.getDisplayMetrics();
        final float density = metrics.density;
        final int minDimensionPx = Math.min(metrics.widthPixels, metrics.heightPixels);
        return (int) (minDimensionPx / density);
    }
}
