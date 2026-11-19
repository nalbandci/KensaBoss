
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.AnswerOptionRepository;
import com.kensaboss.app.data.repository.BookRepository;
import com.kensaboss.app.data.repository.CorrectAnswerRepository;
import com.kensaboss.app.data.repository.QuestionRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.AnswerOption;
import com.kensaboss.app.models.BookCatalogItem;
import com.kensaboss.app.models.CorrectAnswer;
import com.kensaboss.app.models.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrainingBookSelectionActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout booksListContainer;
    private TextView tvEmpty;

    private BookRepository bookRepository;
    private QuestionRepository questionRepository;
    private CorrectAnswerRepository correctAnswerRepository;
    private AnswerOptionRepository answerOptionRepository;

    private final List<TrainingBookItem> availableBooks =
            new ArrayList<>();

    // =====================================================
    // 1. ЛОКАЛИЗАЦИЯ И РАЗМЕР ШРИФТА
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

        setContentView(R.layout.activity_training_book_selection);

        initializeViews();
        initializeRepositories();
        setupClickListeners();

        loadBooks();
    }

    // =====================================================
    // 3. ИНИЦИАЛИЗАЦИЯ ЭЛЕМЕНТОВ
    // =====================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnTrainingBooksBack);

        booksListContainer =
                findViewById(R.id.trainingBooksListContainer);

        tvEmpty = findViewById(R.id.tvTrainingBooksEmpty);
    }

    // =====================================================
    // 4. ИНИЦИАЛИЗАЦИЯ REPOSITORIES
    // =====================================================

    private void initializeRepositories() {

        bookRepository = new BookRepository(this);

        questionRepository = new QuestionRepository(this);

        correctAnswerRepository =
                new CorrectAnswerRepository(this);

        answerOptionRepository =
                new AnswerOptionRepository(this);
    }

    // =====================================================
    // 5. ОБРАБОТКА НАЖАТИЙ
    // =====================================================

    private void setupClickListeners() {

        btnBack.setOnClickListener(view -> finish());
    }

    // =====================================================
    // 6. ЗАГРУЗКА УЧЕБНИКОВ
    // =====================================================

    private void loadBooks() {

        booksListContainer.removeAllViews();
        availableBooks.clear();

        List<BookCatalogItem> catalogBooks =
                bookRepository.getAllCatalogBooks();

        for (BookCatalogItem catalogBook : catalogBooks) {

            List<Question> questions =
                    questionRepository.getQuestionsByBookId(
                            catalogBook.getId()
                    );

            int verifiedQuestionCount = 0;

            for (Question question : questions) {

                if (isQuestionAvailableForTraining(question)) {
                    verifiedQuestionCount++;
                }
            }

            if (verifiedQuestionCount > 0) {

                TrainingBookItem item = new TrainingBookItem(
                        catalogBook,
                        verifiedQuestionCount
                );

                availableBooks.add(item);
            }
        }

        displayBooks();
    }

    // =====================================================
    // 7. ПРОВЕРКА ВОПРОСА ДЛЯ ТРЕНИРОВКИ
    // =====================================================

    private boolean isQuestionAvailableForTraining(
            Question question
    ) {

        if (question == null) {
            return false;
        }

        String questionType = question.getQuestionType();

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
    // 8. ОТОБРАЖЕНИЕ СПИСКА УЧЕБНИКОВ
    // =====================================================

    private void displayBooks() {

        booksListContainer.removeAllViews();

        if (availableBooks.isEmpty()) {

            tvEmpty.setVisibility(View.VISIBLE);

            tvEmpty.setText(getLocalizedText(
                    "Пока нет учебников с проверенными вопросами.",
                    "There are no books with verified questions yet.",
                    "Առայժմ ստուգված հարցերով դասագրքեր չկան։"
            ));

            booksListContainer.addView(tvEmpty);

            return;
        }

        tvEmpty.setVisibility(View.GONE);

        for (TrainingBookItem item : availableBooks) {

            View bookCard = createBookCard(item);

            booksListContainer.addView(bookCard);
        }
    }

    // =====================================================
    // 9. СОЗДАНИЕ КАРТОЧКИ УЧЕБНИКА
    // =====================================================

    private View createBookCard(TrainingBookItem item) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackgroundResource(R.drawable.bg_card_surface);

        card.setClickable(true);
        card.setFocusable(true);

        card.setForeground(
                getDrawable(android.R.drawable.list_selector_background)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);

        card.setLayoutParams(cardParams);

        // Название учебника.

        TextView title = new TextView(this);

        title.setText(item.book.getTitle());

        title.setTextColor(
                getColor(R.color.kb_text_primary)
        );

        title.setTextSize(18);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        card.addView(title);

        // Автор учебника.

        String author = item.book.getAuthor();

        if (author != null && !author.trim().isEmpty()) {

            TextView authorView = new TextView(this);

            authorView.setText(author);

            authorView.setTextColor(
                    getColor(R.color.kb_text_secondary)
            );

            authorView.setTextSize(14);

            LinearLayout.LayoutParams authorParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            authorParams.topMargin = dp(6);

            authorView.setLayoutParams(authorParams);

            card.addView(authorView);
        }

        // Количество проверенных вопросов.

        TextView questionsCount = new TextView(this);

        questionsCount.setText(
                getLocalizedText(
                        "Доступно вопросов для тренировки: "
                                + item.verifiedQuestionCount,

                        "Available training questions: "
                                + item.verifiedQuestionCount,

                        "Ստուգված հարցեր մարզման համար՝ "
                                + item.verifiedQuestionCount
                )
        );

        questionsCount.setTextColor(
                getColor(R.color.kb_text_secondary)
        );

        questionsCount.setTextSize(14);

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        countParams.topMargin = dp(12);

        questionsCount.setLayoutParams(countParams);

        card.addView(questionsCount);

        // Переход к настройке тренировки.

        card.setOnClickListener(view -> openTrainingConfiguration(item));

        return card;
    }

    // =====================================================
    // 10. ПЕРЕХОД К НАСТРОЙКЕ ТРЕНИРОВКИ
    // =====================================================

    private void openTrainingConfiguration(TrainingBookItem item) {

        if (item == null || item.book == null) {
            return;
        }

        Intent intent = new Intent(
                TrainingBookSelectionActivity.this,
                TrainingConfigurationActivity.class
        );

        intent.putExtra(
                TrainingConfigurationActivity.EXTRA_BOOK_ID,
                item.book.getId()
        );

        intent.putExtra(
                TrainingConfigurationActivity.EXTRA_BOOK_TITLE,
                item.book.getTitle()
        );

        startActivity(intent);
    }

    // =====================================================
    // 11. ЛОКАЛИЗАЦИЯ
    // =====================================================

    private String getLocalizedText(
            String russian,
            String english,
            String armenian
    ) {

        String language = Locale.getDefault().getLanguage();

        if ("en".equals(language)) {
            return english;
        }

        if ("hy".equals(language)) {
            return armenian;
        }

        return russian;
    }

    // =====================================================
    // 12. ПЕРЕВОД DP В PX
    // =====================================================

    private int dp(float value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    // =====================================================
    // 13. ЗАКРЫТИЕ REPOSITORIES
    // =====================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bookRepository != null) {
            bookRepository.close();
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
    }

    // =====================================================
    // 14. ВНУТРЕННЯЯ МОДЕЛЬ КАРТОЧКИ
    // =====================================================

    private static class TrainingBookItem {

        final BookCatalogItem book;
        final int verifiedQuestionCount;

        TrainingBookItem(
                BookCatalogItem book,
                int verifiedQuestionCount
        ) {

            this.book = book;
            this.verifiedQuestionCount = verifiedQuestionCount;
        }
    }
}
