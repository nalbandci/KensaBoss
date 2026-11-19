package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private LinearLayout cardContinueLearning;
    private LinearLayout cardBooks;
    private LinearLayout cardTraining;
    private LinearLayout cardReference;
    private LinearLayout cardStatistics;
    private ImageButton btnSettings;

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

        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
    }

    // =====================================================
    // 1. ИНИЦИАЛИЗАЦИЯ ЭЛЕМЕНТОВ
    // =====================================================

    private void initializeViews() {

        cardContinueLearning = findViewById(R.id.cardContinueLearning);
        cardBooks = findViewById(R.id.cardBooks);
        cardTraining = findViewById(R.id.cardTraining);
        cardReference = findViewById(R.id.cardReference);
        cardStatistics = findViewById(R.id.cardStatistics);

        btnSettings = findViewById(R.id.btnSettings);
    }

    // =====================================================
    // 2. ОБРАБОТКА НАЖАТИЙ
    // =====================================================

    private void openHub(String mode) { Intent intent = new Intent(this, LearningHubActivity.class); intent.putExtra("mode", mode); startActivity(intent); }
    private void setupClickListeners() {

        // Продолжение последнего занятия.
        cardContinueLearning.setOnClickListener(view -> {
            try (com.kensaboss.app.data.repository.TrainingSessionRepository repository =
                         new com.kensaboss.app.data.repository.TrainingSessionRepository(this)) {
                java.util.List<com.kensaboss.app.models.TrainingSession> sessions =
                        repository.getSessionsByStatus(com.kensaboss.app.models.TrainingSession.STATUS_IN_PROGRESS);
                if (sessions.isEmpty()) {
                    startActivity(new Intent(this, TrainingBookSelectionActivity.class));
                } else {
                    Intent intent = new Intent(this, TrainingQuestionActivity.class);
                    intent.putExtra(TrainingQuestionActivity.EXTRA_SESSION_ID, sessions.get(0).getId());
                    startActivity(intent);
                }
            }
        });

        // Каталог учебников.
        cardBooks.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    BooksActivity.class
            );

            startActivity(intent);
        });

        // Режим тренировки.
        cardTraining.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    TrainingBookSelectionActivity.class
            );

            startActivity(intent);
        });

        // Справочник.
        cardReference.setOnClickListener(view -> {
            openHub("reference");
        });

        // Статистика.
        cardStatistics.setOnClickListener(view -> {
            openHub("history");
        });

        // Настройки.
        btnSettings.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });
    }
}