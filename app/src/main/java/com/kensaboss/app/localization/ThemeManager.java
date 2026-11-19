
package com.kensaboss.app.localization;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private static final String PREFERENCES_NAME = "kensaboss_settings";
    private static final String KEY_THEME = "app_theme";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    private ThemeManager() {
        // Utility class.
    }

    public static String getSavedTheme(Context context) {
        SharedPreferences preferences =
                context.getApplicationContext().getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(KEY_THEME, THEME_SYSTEM);
    }

    public static void saveTheme(Context context, String theme) {
        validateTheme(theme);

        SharedPreferences preferences =
                context.getApplicationContext().getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(KEY_THEME, theme)
                .apply();
    }

    public static void applyTheme(Context context) {
        String theme = getSavedTheme(context);

        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
                break;

            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;
        }
    }

    private static void validateTheme(String theme) {
        if (!THEME_LIGHT.equals(theme)
                && !THEME_DARK.equals(theme)
                && !THEME_SYSTEM.equals(theme)) {

            throw new IllegalArgumentException(
                    "Unsupported theme: " + theme
            );
        }
    }
}
