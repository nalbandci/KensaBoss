
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioGroupTheme;
    private RadioGroup radioGroupLanguage;
    private RadioGroup radioGroupFontSize;

    private TextView tvFontPreview;

    private Button btnSaveSettings;
    private Button btnCancelSettings;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context localizedContext = LocaleManager.wrap(newBase);
        Context fontContext = FontSizeManager.wrap(localizedContext);

        super.attachBaseContext(fontContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        initializeViews();
        loadCurrentSettings();
        setupListeners();
    }

    private void initializeViews() {
        radioGroupTheme = findViewById(R.id.radioGroupTheme);
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        radioGroupFontSize = findViewById(R.id.radioGroupFontSize);

        tvFontPreview = findViewById(R.id.tvFontPreview);

        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnCancelSettings = findViewById(R.id.btnCancelSettings);
    }

    private void loadCurrentSettings() {
        String theme = ThemeManager.getSavedTheme(this);
        String language = LocaleManager.getSavedLanguage(this);
        String fontSize = FontSizeManager.getSavedSize(this);

        if (ThemeManager.THEME_LIGHT.equals(theme)) {
            radioGroupTheme.check(R.id.radioThemeLight);
        } else if (ThemeManager.THEME_DARK.equals(theme)) {
            radioGroupTheme.check(R.id.radioThemeDark);
        } else {
            radioGroupTheme.check(R.id.radioThemeSystem);
        }

        if (LocaleManager.LANGUAGE_ARMENIAN.equals(language)) {
            radioGroupLanguage.check(R.id.radioLanguageArmenian);
        } else if (LocaleManager.LANGUAGE_RUSSIAN.equals(language)) {
            radioGroupLanguage.check(R.id.radioLanguageRussian);
        } else if (LocaleManager.LANGUAGE_ENGLISH.equals(language)) {
            radioGroupLanguage.check(R.id.radioLanguageEnglish);
        }

        if (FontSizeManager.SIZE_SMALL.equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioFontSmall);
        } else if (FontSizeManager.SIZE_LARGE.equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioFontLarge);
        } else if (FontSizeManager.SIZE_EXTRA_LARGE.equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioFontExtraLarge);
        } else {
            radioGroupFontSize.check(R.id.radioFontStandard);
        }

        updateFontPreview();
    }

    private void setupListeners() {
        radioGroupFontSize.setOnCheckedChangeListener(
                (group, checkedId) -> updateFontPreview()
        );

        btnSaveSettings.setOnClickListener(view -> saveSettings());

        btnCancelSettings.setOnClickListener(view -> finish());
    }

    private void updateFontPreview() {
        int selectedId = radioGroupFontSize.getCheckedRadioButtonId();

        float scale;

        if (selectedId == R.id.radioFontSmall) {
            scale = 0.85f;
        } else if (selectedId == R.id.radioFontLarge) {
            scale = 1.15f;
        } else if (selectedId == R.id.radioFontExtraLarge) {
            scale = 1.30f;
        } else {
            scale = 1.0f;
        }

        tvFontPreview.setTextSize(
                android.util.TypedValue.COMPLEX_UNIT_SP,
                18f * scale
        );
    }

    private void saveSettings() {
        int selectedThemeId = radioGroupTheme.getCheckedRadioButtonId();
        int selectedLanguageId = radioGroupLanguage.getCheckedRadioButtonId();
        int selectedFontId = radioGroupFontSize.getCheckedRadioButtonId();

        if (selectedThemeId == -1
                || selectedLanguageId == -1
                || selectedFontId == -1) {
            return;
        }

        String theme;
        String language;
        String fontSize;

        if (selectedThemeId == R.id.radioThemeLight) {
            theme = ThemeManager.THEME_LIGHT;
        } else if (selectedThemeId == R.id.radioThemeDark) {
            theme = ThemeManager.THEME_DARK;
        } else {
            theme = ThemeManager.THEME_SYSTEM;
        }

        if (selectedLanguageId == R.id.radioLanguageArmenian) {
            language = LocaleManager.LANGUAGE_ARMENIAN;
        } else if (selectedLanguageId == R.id.radioLanguageRussian) {
            language = LocaleManager.LANGUAGE_RUSSIAN;
        } else {
            language = LocaleManager.LANGUAGE_ENGLISH;
        }

        if (selectedFontId == R.id.radioFontSmall) {
            fontSize = FontSizeManager.SIZE_SMALL;
        } else if (selectedFontId == R.id.radioFontLarge) {
            fontSize = FontSizeManager.SIZE_LARGE;
        } else if (selectedFontId == R.id.radioFontExtraLarge) {
            fontSize = FontSizeManager.SIZE_EXTRA_LARGE;
        } else {
            fontSize = FontSizeManager.SIZE_STANDARD;
        }

        ThemeManager.saveTheme(this, theme);
        LocaleManager.saveLanguage(this, language);
        FontSizeManager.saveSize(this, fontSize);

        ThemeManager.applyTheme(this);

        openMainActivity();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
