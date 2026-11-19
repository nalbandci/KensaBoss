
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;

public class LanguageSelectionActivity extends AppCompatActivity {

    private RadioGroup radioGroupLanguages;
    private Button btnContinue;

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

        if (LocaleManager.hasSelectedLanguage(this)) {
            openMainActivity();
            return;
        }

        setContentView(R.layout.activity_language_selection);

        radioGroupLanguages = findViewById(R.id.radioGroupLanguages);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(view -> saveSelectedLanguage());
    }

    private void saveSelectedLanguage() {
        int selectedId = radioGroupLanguages.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(
                    this,
                    "Сначала выберите язык",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String language;

        if (selectedId == R.id.radioArmenian) {
            language = LocaleManager.LANGUAGE_ARMENIAN;
        } else if (selectedId == R.id.radioRussian) {
            language = LocaleManager.LANGUAGE_RUSSIAN;
        } else if (selectedId == R.id.radioEnglish) {
            language = LocaleManager.LANGUAGE_ENGLISH;
        } else {
            return;
        }

        LocaleManager.saveLanguage(this, language);
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
