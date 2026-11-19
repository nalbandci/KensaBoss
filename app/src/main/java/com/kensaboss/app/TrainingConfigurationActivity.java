
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.AnswerOptionRepository;
import com.kensaboss.app.data.repository.BookPartRepository;
import com.kensaboss.app.data.repository.ChapterRepository;
import com.kensaboss.app.data.repository.CorrectAnswerRepository;
import com.kensaboss.app.data.repository.QuestionRepository;
import com.kensaboss.app.data.repository.TrainingSessionRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.AnswerOption;
import com.kensaboss.app.models.BookPart;
import com.kensaboss.app.models.Chapter;
import com.kensaboss.app.models.CorrectAnswer;
import com.kensaboss.app.models.Question;
import com.kensaboss.app.models.TrainingSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrainingConfigurationActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID =
            "com.kensaboss.app.EXTRA_TRAINING_BOOK_ID";

    public static final String EXTRA_BOOK_TITLE =
            "com.kensaboss.app.EXTRA_TRAINING_BOOK_TITLE";

    private ImageButton btnBack;

    private TextView tvBookTitle;
    private TextView tvAvailableQuestions;
    private TextView tvChaptersDescription;

    private EditText etQuestionCount;

    private Button btnSelectAll;
    private Button btnClearSelection;
    private Button btnStartTraining;

    private RadioGroup rgQuestionOrder;
    private RadioGroup rgFeedbackMode;

    private LinearLayout chaptersContainer;

    private long bookId;
    private String bookTitle;

    private BookPartRepository bookPartRepository;
    private ChapterRepository chapterRepository;
    private QuestionRepository questionRepository;
    private CorrectAnswerRepository correctAnswerRepository;
    private AnswerOptionRepository answerOptionRepository;
    private TrainingSessionRepository trainingSessionRepository;

    private final Map<Long, CheckBox> chapterCheckBoxes =
            new LinkedHashMap<>();

    private final Map<Long, List<Long>> chapterQuestionIds =
            new LinkedHashMap<>();

    private int availableQuestionCount = 0;

    // =====================================================
    // 1. ЛОКАЛИЗАЦИЯ И ТЕМА
    // =====================================================

    @Override
    protected void attachBaseContext(Context newBase) {

        Context localizedContext = LocaleManager.wrap(newBase);
        Context fontContext = FontSizeManager.wrap(localizedContext);

        super.attachBaseContext(fontContext);
    }

    // =====================================================
    // 2. СОЗДАНИЕ ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_training_configuration);

        bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);

        if (bookId <= 0) {

            Toast.makeText(
                    this,
                    "Не удалось определить выбранный учебник.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            bookTitle = "Учебник";
        }

        initializeViews();
        initializeRepositories();
        setupClickListeners();

        tvBookTitle.setText(bookTitle);

        loadBookStructure();
        updateAvailableQuestionCount();
    }

    // =====================================================
    // 3. ИНИЦИАЛИЗАЦИЯ ЭЛЕМЕНТОВ
    // =====================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnTrainingConfigBack);

        tvBookTitle =
                findViewById(R.id.tvTrainingConfigBookTitle);

        tvAvailableQuestions =
                findViewById(R.id.tvTrainingAvailableQuestions);

        tvChaptersDescription =
                findViewById(R.id.tvTrainingConfigChaptersDescription);

        etQuestionCount =
                findViewById(R.id.etTrainingQuestionCount);

        btnSelectAll =
                findViewById(R.id.btnTrainingSelectAll);

        btnClearSelection =
                findViewById(R.id.btnTrainingClearSelection);

        btnStartTraining =
                findViewById(R.id.btnStartTraining);

        rgQuestionOrder =
                findViewById(R.id.rgTrainingQuestionOrder);

        rgFeedbackMode =
                findViewById(R.id.rgTrainingFeedbackMode);

        chaptersContainer =
                findViewById(R.id.trainingConfigChaptersContainer);
    }

    // =====================================================
    // 4. ИНИЦИАЛИЗАЦИЯ REPOSITORIES
    // =====================================================

    private void initializeRepositories() {

        bookPartRepository = new BookPartRepository(this);

        chapterRepository = new ChapterRepository(this);

        questionRepository = new QuestionRepository(this);

        correctAnswerRepository =
                new CorrectAnswerRepository(this);

        answerOptionRepository =
                new AnswerOptionRepository(this);

        trainingSessionRepository =
                new TrainingSessionRepository(this);
    }

    // =====================================================
    // 5. ОБРАБОТКА НАЖАТИЙ
    // =====================================================

    private void setupClickListeners() {

        btnBack.setOnClickListener(view -> finish());

        btnSelectAll.setOnClickListener(view -> {

            for (CheckBox checkBox : chapterCheckBoxes.values()) {
                checkBox.setChecked(true);
            }
        });

        btnClearSelection.setOnClickListener(view -> {

            for (CheckBox checkBox : chapterCheckBoxes.values()) {
                checkBox.setChecked(false);
            }
        });

        btnStartTraining.setOnClickListener(
                view -> validateConfiguration()
        );
    }

    // =====================================================
    // 6. ЗАГРУЗКА СТРУКТУРЫ УЧЕБНИКА
    // =====================================================

    private void loadBookStructure() {

        chaptersContainer.removeAllViews();

        chapterCheckBoxes.clear();
        chapterQuestionIds.clear();

        List<BookPart> parts =
                bookPartRepository.getBookPartsByBookId(bookId);

        if (parts == null || parts.isEmpty()) {

            showEmptyStructureMessage(
                    "В этом учебнике пока нет доступных частей."
            );

            return;
        }

        int totalChapters = 0;

        for (BookPart part : parts) {

            if (part == null) {
                continue;
            }

            addPartHeader(part.getTitle());

            List<Chapter> chapters =
                    chapterRepository.getChaptersByBookPartId(
                            part.getId()
                    );

            if (chapters == null || chapters.isEmpty()) {

                addEmptyPartMessage();

                continue;
            }

            for (Chapter chapter : chapters) {

                if (chapter == null) {
                    continue;
                }

                addChapterCheckBox(chapter);

                totalChapters++;
            }
        }

        if (totalChapters == 0) {

            showEmptyStructureMessage(
                    "В этом учебнике пока нет глав для тренировки."
            );

        } else {

            tvChaptersDescription.setText(
                    "Отметьте главы, вопросы из которых хотите включить в тренировку."
            );
        }
    }

    // =====================================================
    // 7. ЗАГОЛОВОК ЧАСТИ
    // =====================================================

    private void addPartHeader(String title) {

        TextView header = new TextView(this);

        header.setText(
                title == null ? "Часть учебника" : title
        );

        header.setTextColor(
                getResources().getColor(R.color.kb_text_primary)
        );

        header.setTextSize(17);

        header.setTypeface(null, Typeface.BOLD);

        header.setPadding(
                dp(4),
                dp(16),
                dp(4),
                dp(8)
        );

        chaptersContainer.addView(header);
    }

    // =====================================================
    // 8. ДОБАВЛЕНИЕ ГЛАВЫ
    // =====================================================

    private void addChapterCheckBox(Chapter chapter) {

        CheckBox checkBox = new CheckBox(this);

        String chapterTitle = chapter.getTitle();

        if (chapterTitle == null || chapterTitle.trim().isEmpty()) {

            chapterTitle = "Глава " + chapter.getChapterNumber();
        }

        checkBox.setText(chapterTitle);

        checkBox.setTextColor(
                getResources().getColor(R.color.kb_text_primary)
        );

        checkBox.setTextSize(15);

        checkBox.setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(8)
        );

        long chapterId = chapter.getId();

        chapterCheckBoxes.put(chapterId, checkBox);

        List<Long> questionIds =
                getVerifiedQuestionIds(chapterId);

        chapterQuestionIds.put(chapterId, questionIds);

        checkBox.setChecked(true);

        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        updateAvailableQuestionCount()
        );

        chaptersContainer.addView(checkBox);
    }

    // =====================================================
    // 9. ПОЛУЧЕНИЕ ПРОВЕРЕННЫХ ВОПРОСОВ ГЛАВЫ
    // =====================================================

    private List<Long> getVerifiedQuestionIds(long chapterId) {

        List<Long> result = new ArrayList<>();

        List<Question> questions =
                questionRepository.getQuestionsByChapterId(chapterId);

        if (questions == null) {
            return result;
        }

        for (Question question : questions) {

            if (question != null
                    && isQuestionAvailableForTraining(question)) {

                result.add(question.getId());
            }
        }

        return result;
    }

    // =====================================================
    // 10. ПРОВЕРКА ВОПРОСА ДЛЯ ТРЕНИРОВКИ
    // =====================================================

    private boolean isQuestionAvailableForTraining(
            Question question
    ) {

        String questionType = question.getQuestionType();

        if (Question.TYPE_NUMERIC.equals(questionType) || Question.TYPE_TEXT.equals(questionType) || Question.TYPE_MATCHING.equals(questionType) || Question.TYPE_SEQUENCE.equals(questionType)) {
            if (!Question.KEY_VERIFIED.equals(question.getSourceKeyStatus())) return false;
            List<CorrectAnswer> keys = correctAnswerRepository.getCorrectAnswersByQuestionId(question.getId());
            if (keys.isEmpty()) return false;
            for (CorrectAnswer key : keys) if (!CorrectAnswer.STATUS_VERIFIED.equals(key.getVerificationStatus()) || key.getAnswerValue() == null || CorrectAnswer.TYPE_OPTION.equals(key.getAnswerType())) return false;
            return true;
        }
        boolean supportedType =
                Question.TYPE_SINGLE_CHOICE.equals(questionType)
                        || Question.TYPE_MULTIPLE_CHOICE.equals(questionType)
                        || Question.TYPE_TRUE_FALSE.equals(questionType);

        if (!supportedType) {
            return false;
        }

        if (!Question.KEY_VERIFIED.equals(
                question.getSourceKeyStatus()
        )) {
            return false;
        }

        List<CorrectAnswer> correctAnswers =
                correctAnswerRepository.getCorrectAnswersByQuestionId(
                        question.getId()
                );

        if (correctAnswers == null || correctAnswers.isEmpty()) {
            return false;
        }

        List<AnswerOption> options =
                answerOptionRepository.getAnswerOptionsByQuestionId(
                        question.getId()
                );

        if (options == null || options.isEmpty()) {
            return false;
        }

        for (CorrectAnswer correctAnswer : correctAnswers) {

            if (correctAnswer == null) {
                return false;
            }

            if (!CorrectAnswer.STATUS_VERIFIED.equals(
                    correctAnswer.getVerificationStatus()
            )) {
                return false;
            }

            if (!CorrectAnswer.TYPE_OPTION.equals(
                    correctAnswer.getAnswerType()
            )) {
                return false;
            }

            if (correctAnswer.getOptionId() == null) {
                return false;
            }

            boolean optionExists = false;

            for (AnswerOption option : options) {

                if (option != null
                        && option.getId() == correctAnswer.getOptionId()) {

                    optionExists = true;

                    break;
                }
            }

            if (!optionExists) {
                return false;
            }
        }

        return true;
    }

    // =====================================================
    // 11. ПЕРЕСЧЁТ ДОСТУПНЫХ ВОПРОСОВ
    // =====================================================

    private void updateAvailableQuestionCount() {

        Set<Long> uniqueQuestionIds = new LinkedHashSet<>();

        for (Map.Entry<Long, CheckBox> entry :
                chapterCheckBoxes.entrySet()) {

            CheckBox checkBox = entry.getValue();

            if (checkBox == null || !checkBox.isChecked()) {
                continue;
            }

            List<Long> ids =
                    chapterQuestionIds.get(entry.getKey());

            if (ids != null) {
                uniqueQuestionIds.addAll(ids);
            }
        }

        availableQuestionCount = uniqueQuestionIds.size();

        tvAvailableQuestions.setText(
                String.valueOf(availableQuestionCount)
        );

        if (availableQuestionCount == 0) {

            etQuestionCount.setText("");

            btnStartTraining.setEnabled(false);

        } else {

            String currentValue =
                    etQuestionCount.getText().toString().trim();

            if (currentValue.isEmpty()) {

                etQuestionCount.setText(
                        String.valueOf(availableQuestionCount)
                );

            } else {

                try {

                    int requestedCount =
                            Integer.parseInt(currentValue);

                    if (requestedCount > availableQuestionCount) {

                        etQuestionCount.setText(
                                String.valueOf(availableQuestionCount)
                        );
                    }

                } catch (NumberFormatException ignored) {

                    etQuestionCount.setText(
                            String.valueOf(availableQuestionCount)
                    );
                }
            }

            btnStartTraining.setEnabled(true);
        }
    }

    // =====================================================
    // 12. ПРОВЕРКА НАСТРОЕК
    // =====================================================

    private void validateConfiguration() {

        if (availableQuestionCount <= 0) {

            Toast.makeText(
                    this,
                    "Выбери хотя бы одну главу с доступными вопросами.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String countText =
                etQuestionCount.getText().toString().trim();

        if (countText.isEmpty()) {

            etQuestionCount.setError(
                    "Укажи количество вопросов"
            );

            return;
        }

        int requestedCount;

        try {

            requestedCount = Integer.parseInt(countText);

        } catch (NumberFormatException e) {

            etQuestionCount.setError(
                    "Некорректное число"
            );

            return;
        }

        if (requestedCount <= 0) {

            etQuestionCount.setError(
                    "Количество должно быть больше нуля"
            );

            return;
        }

        if (requestedCount > availableQuestionCount) {

            etQuestionCount.setError(
                    "Доступно только " + availableQuestionCount
                            + " вопросов"
            );

            return;
        }

        int orderId =
                rgQuestionOrder.getCheckedRadioButtonId();

        int feedbackId =
                rgFeedbackMode.getCheckedRadioButtonId();

        if (orderId == -1 || feedbackId == -1) {

            Toast.makeText(
                    this,
                    "Выбери порядок вопросов и режим проверки.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        createTrainingSession(
                requestedCount,
                orderId,
                feedbackId
        );
    }

    // =====================================================
    // 13. СОЗДАНИЕ ТРЕНИРОВОЧНОЙ СЕССИИ
    // =====================================================

    private void createTrainingSession(
            int requestedCount,
            int orderId,
            int feedbackId
    ) {

        // Получаем вопросы только из выбранных глав.
        Set<Long> uniqueQuestionIds = new LinkedHashSet<>();

        for (Map.Entry<Long, CheckBox> entry :
                chapterCheckBoxes.entrySet()) {

            CheckBox checkBox = entry.getValue();

            if (checkBox == null || !checkBox.isChecked()) {
                continue;
            }

            List<Long> ids =
                    chapterQuestionIds.get(entry.getKey());

            if (ids != null) {
                uniqueQuestionIds.addAll(ids);
            }
        }

        if (uniqueQuestionIds.size() < requestedCount) {

            Toast.makeText(
                    this,
                    "Количество доступных вопросов изменилось. Проверь настройки.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Создаём изменяемый список вопросов.
        List<Long> selectedQuestionIds =
                new ArrayList<>(uniqueQuestionIds);

        // Определяем порядок вопросов.
        String questionOrder;

        if (orderId == R.id.rbTrainingRandom) {

            questionOrder = TrainingSession.ORDER_RANDOM;

            Collections.shuffle(selectedQuestionIds);

        } else if (orderId == R.id.rbTrainingSequential) {

            questionOrder = TrainingSession.ORDER_SEQUENTIAL;

        } else {

            Toast.makeText(
                    this,
                    "Не удалось определить порядок вопросов.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Оставляем только указанное количество вопросов.
        if (selectedQuestionIds.size() > requestedCount) {

            selectedQuestionIds = new ArrayList<>(
                    selectedQuestionIds.subList(0, requestedCount)
            );
        }

        // Определяем режим проверки.
        String feedbackMode;

        if (feedbackId == R.id.rbTrainingImmediate) {

            feedbackMode = TrainingSession.FEEDBACK_IMMEDIATE;

        } else if (feedbackId == R.id.rbTrainingDeferred) {

            feedbackMode = TrainingSession.FEEDBACK_DEFERRED;

        } else {

            Toast.makeText(
                    this,
                    "Не удалось определить режим проверки.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Создаём объект тренировочной сессии.
        TrainingSession session = new TrainingSession(
                feedbackMode,
                questionOrder,
                selectedQuestionIds.size()
        );

        // Сохраняем сессию и вопросы одной транзакцией.
        try {

            long sessionId =
                    trainingSessionRepository.insertSession(
                            session,
                            selectedQuestionIds
                    );

            if (sessionId <= 0) {

                Toast.makeText(
                        this,
                        "Не удалось создать тренировочную сессию.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            // Открываем экран первого вопроса.
            Intent intent = new Intent(
                    TrainingConfigurationActivity.this,
                    TrainingQuestionActivity.class
            );

            intent.putExtra(
                    TrainingQuestionActivity.EXTRA_SESSION_ID,
                    sessionId
            );

            startActivity(intent);

            // Не возвращаемся к настройке уже созданной сессии.
            finish();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Ошибка создания тренировки: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // 14. ПУСТАЯ СТРУКТУРА УЧЕБНИКА
    // =====================================================

    private void showEmptyStructureMessage(String message) {

        TextView textView = new TextView(this);

        textView.setText(message);

        textView.setTextColor(
                getResources().getColor(R.color.kb_text_secondary)
        );

        textView.setTextSize(15);

        textView.setGravity(Gravity.CENTER);

        textView.setPadding(
                dp(16),
                dp(20),
                dp(16),
                dp(20)
        );

        chaptersContainer.addView(textView);

        tvChaptersDescription.setText(message);

        btnStartTraining.setEnabled(false);
    }

    private void addEmptyPartMessage() {

        TextView textView = new TextView(this);

        textView.setText(
                "В этой части пока нет глав."
        );

        textView.setTextColor(
                getResources().getColor(R.color.kb_text_secondary)
        );

        textView.setTextSize(13);

        textView.setPadding(
                dp(12),
                dp(4),
                dp(12),
                dp(8)
        );

        chaptersContainer.addView(textView);
    }

    // =====================================================
    // 15. DP В PX
    // =====================================================

    private int dp(float value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    // =====================================================
    // 16. ЗАКРЫТИЕ REPOSITORIES
    // =====================================================

    @Override
    protected void onDestroy() {

        if (bookPartRepository != null) {
            bookPartRepository.close();
        }

        if (chapterRepository != null) {
            chapterRepository.close();
        }

        if (questionRepository != null) {
            questionRepository.close();
        }

        if (correctAnswerRepository != null) {
            correctAnswerRepository.close();
        }

        if (answerOptionRepository != null) {
            answerOptionRepository.close();
        }

        if (trainingSessionRepository != null) {
            trainingSessionRepository.close();
        }

        super.onDestroy();
    }
}
