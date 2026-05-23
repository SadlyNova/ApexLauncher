package com.movtery.zalithlauncher.ui.utils;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.View;

public class ThemeUtils {

    /**
     * Completely forces a dark theme across any activity window canvas layout context.
     * This overrides system resources to stop Light Mode white background bleed.
     *
     * @param activity The target activity workspace window context to style.
     */
    public static void applyDarkTheme(Activity activity) {
        if (activity == null) return;

        // 1. Target the application custom dark theme resource directly
        // Uses dynamic resource identifier lookup to keep compilation secure
        int themeId = activity.getResources().getIdentifier("AppTheme", "style", activity.getPackageName());
        if (themeId != 0) {
            activity.setTheme(themeId);
        }

        // 2. Clear window canvas attributes and force dark charcoal hues
        Window window = activity.getWindow();
        if (window != null) {
            // Force layout canvas background layer to #181818 dark
            window.setBackgroundDrawable(new ColorDrawable(0xFF181818));
            
            // Hardcode system structural bars to match layout dark colors
            window.setStatusBarColor(0xFF242424);
            window.setNavigationBarColor(0xFF181818);

            // 3. Force system icons and status text to remain white/light-colored
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller = window.getInsetsController();
                if (controller != null) {
                    // Clearing these flags means system bars do NOT adapt to Light Mode icons
                    controller.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                    controller.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
                }
            } else {
                // Legacy system fallback behavior for older Android versions
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
                decorView.setSystemUiVisibility(flags);
            }
        }
    }
}
