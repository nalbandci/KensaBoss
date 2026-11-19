
package com.kensaboss.app.localization;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public final class FontSizeManager {

    private static final String PREFERENCES_NAME = "kensaboss_settings";
    private static final String KEY_FONT_SIZE = "app_font_size";

    public static final String SIZE_SMALL = "small";
    public static final String SIZE_STANDARD = "standard";
    public static final String SIZE_LARGE = "large";
    public static final String SIZE_EXTRA_LARGE = "extra_large";

    private FontSizeManager() {
        // Utility class.
    }

    public static String getSavedSize(Context context) {
        SharedPreferences preferences =
                context.getApplicationContext().getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(KEY_FONT_SIZE, SIZE_STANDARD);
    }

    public static void saveSize(Context context, String size) {
        validateSize(size);

        SharedPreferences preferences =
                context.getApplicationContext().getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(KEY_FONT_SIZE, size)
                .apply();
    }

    public static float getScale(Context context) {
        String size = getSavedSize(context);

        switch (size) {
            case SIZE_SMALL:
                return 0.85f;

            case SIZE_STANDARD:
                return 1.0f;

            case SIZE_LARGE:
                return 1.15f;

            case SIZE_EXTRA_LARGE:
                return 1.30f;

            default:
                return 1.0f;
        }
    }

    public static Context wrap(Context context) {
        Configuration configuration =
                new Configuration(context.getResources().getConfiguration());

        float systemFontScale = configuration.fontScale;
        float applicationFontScale = getScale(context);

        configuration.fontScale =
                systemFontScale * applicationFontScale;

        return context.createConfigurationContext(configuration);
    }

    private static void validateSize(String size) {
        if (!SIZE_SMALL.equals(size)
                && !SIZE_STANDARD.equals(size)
                && !SIZE_LARGE.equals(size)
                && !SIZE_EXTRA_LARGE.equals(size)) {

            throw new IllegalArgumentException(
                    "Unsupported font size: " + size
            );
        }
    }
}
