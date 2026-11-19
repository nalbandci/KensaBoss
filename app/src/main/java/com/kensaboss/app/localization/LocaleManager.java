
package com.kensaboss.app.localization;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public final class LocaleManager {

    private static final String PREFERENCES_NAME = "kensaboss_settings";
    private static final String KEY_LANGUAGE = "app_language";

    public static final String LANGUAGE_ARMENIAN = "hy";
    public static final String LANGUAGE_RUSSIAN = "ru";
    public static final String LANGUAGE_ENGLISH = "en";

    private LocaleManager() {
        // Utility class.
    }

    /**
     * Возвращает сохранённый язык.
     * Если пользователь ещё не выбирал язык, возвращает null.
     */
    public static String getSavedLanguage(Context context) {
        SharedPreferences preferences =
                context.getApplicationContext().getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(KEY_LANGUAGE, null);
    }

    /**
     * Сохраняет выбранный язык.
     */
    public static void saveLanguage(Context context, String language) {
        validateLanguage(language);

        SharedPreferences preferences =
                context.getApplicationContext().getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(KEY_LANGUAGE, language)
                .apply();
    }

    /**
     * Проверяет, выбирал ли пользователь язык ранее.
     */
    public static boolean hasSelectedLanguage(Context context) {
        return getSavedLanguage(context) != null;
    }

    /**
     * Применяет сохранённый язык к Context.
     */
    public static Context wrap(Context context) {
        String language = getSavedLanguage(context);

        if (language == null) {
            return context;
        }

        Locale locale = Locale.forLanguageTag(language);
        Locale.setDefault(locale);

        Configuration configuration =
                new Configuration(context.getResources().getConfiguration());

        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    /**
     * Проверяет допустимость языка.
     */
    private static void validateLanguage(String language) {
        if (!LANGUAGE_ARMENIAN.equals(language)
                && !LANGUAGE_RUSSIAN.equals(language)
                && !LANGUAGE_ENGLISH.equals(language)) {

            throw new IllegalArgumentException(
                    "Unsupported language: " + language
            );
        }
    }
}
