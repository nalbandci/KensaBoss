package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.QuestionRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.Question;

import java.util.List;

public class QuestionsActivity extends AppCompatActivity {

    public static final String EXTRA_SECTION_ID = "section_id";
    public static final String EXTRA_BOOK_TITLE = "book_title";
    public static final String EXTRA_PART_TITLE = "part_title";
    public static final String EXTRA_CHAPTER_TITLE = "chapter_title";
    public static final String EXTRA_SECTION_TITLE = "section_title";

    private long sectionId;

    private String bookTitle;
    private String partTitle;
    private String chapterTitle;
    private String sectionTitle;

    private LinearLayout questionsListContainer;
    private TextView tvQuestionsEmpty;
    private TextView tvQuestionsCount;

    private QuestionRepository questionRepository;

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
        setContentView(R.layout.activity_questions);

        sectionId = getIntent().getLongExtra(EXTRA_SECTION_ID, -1);

        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);
        partTitle = getIntent().getStringExtra(EXTRA_PART_TITLE);
        chapterTitle = getIntent().getStringExtra(EXTRA_CHAPTER_TITLE);
        sectionTitle = getIntent().getStringExtra(EXTRA_SECTION_TITLE);

        if (sectionId <= 0) {
            finish();
            return;
        }

        if (bookTitle == null) bookTitle = "";
        if (partTitle == null) partTitle = "";
        if (chapterTitle == null) chapterTitle = "";
        if (sectionTitle == null) sectionTitle = "";

        initializeViews();
        setupClickListeners();

        questionRepository = new QuestionRepository(this);

        loadQuestions();
    }

    private void initializeViews() {
        TextView tvBookTitle = findViewById(R.id.tvQuestionsBookTitle);
        TextView tvPartTitle = findViewById(R.id.tvQuestionsPartTitle);
        TextView tvChapterTitle = findViewById(R.id.tvQuestionsChapterTitle);
        TextView tvSectionTitle = findViewById(R.id.tvQuestionsSectionTitle);

        questionsListContainer = findViewById(R.id.questionsListContainer);
        tvQuestionsEmpty = findViewById(R.id.tvQuestionsEmpty);
        tvQuestionsCount = findViewById(R.id.tvQuestionsCount);

        tvBookTitle.setText(bookTitle);
        tvPartTitle.setText(partTitle);
        tvChapterTitle.setText(chapterTitle);
        tvSectionTitle.setText(sectionTitle);
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnQuestionsBack);
        btnBack.setOnClickListener(view -> finish());
    }

    private void loadQuestions() {
        questionsListContainer.removeAllViews();

        List<Question> questions =
                questionRepository.getQuestionsBySectionId(sectionId);

        if (questions == null || questions.isEmpty()) {
            tvQuestionsCount.setText(getQuestionsCountText(0));
            tvQuestionsEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvQuestionsEmpty.setVisibility(View.GONE);
        tvQuestionsCount.setText(
                getQuestionsCountText(questions.size())
        );

        for (Question question : questions) {
            addQuestionCard(question);
        }
    }

    private void addQuestionCard(Question question) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
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

        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView tvQuestionNumber = new TextView(this);
        tvQuestionNumber.setText(getQuestionNumberText(question));
        tvQuestionNumber.setTextColor(getColor(R.color.kb_primary));
        tvQuestionNumber.setTextSize(14);
        tvQuestionNumber.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView tvQuestionText = new TextView(this);
        tvQuestionText.setText(question.getQuestionText());
        tvQuestionText.setTextColor(
                getColor(R.color.kb_text_primary)
        );
        tvQuestionText.setTextSize(16);
        tvQuestionText.setPadding(0, dp(8), 0, 0);

        card.addView(tvQuestionNumber);
        card.addView(tvQuestionText);

        Integer sourcePage = question.getSourcePage();

        if (sourcePage != null) {
            TextView tvSourcePage = new TextView(this);
            tvSourcePage.setText(getSourcePageText(sourcePage));
            tvSourcePage.setTextColor(
                    getColor(R.color.kb_text_secondary)
            );
            tvSourcePage.setTextSize(12);
            tvSourcePage.setPadding(0, dp(10), 0, 0);

            card.addView(tvSourcePage);
        }

        card.setOnClickListener(view -> openQuestion(question));

        questionsListContainer.addView(card);
    }

    private void openQuestion(Question question) {
        Intent intent = new Intent(
                QuestionsActivity.this,
                QuestionDetailsActivity.class
        );

        intent.putExtra(
                QuestionDetailsActivity.EXTRA_QUESTION_ID,
                question.getId()
        );

        intent.putExtra(
                QuestionDetailsActivity.EXTRA_BOOK_TITLE,
                bookTitle
        );

        intent.putExtra(
                QuestionDetailsActivity.EXTRA_PART_TITLE,
                partTitle
        );

        intent.putExtra(
                QuestionDetailsActivity.EXTRA_CHAPTER_TITLE,
                chapterTitle
        );

        intent.putExtra(
                QuestionDetailsActivity.EXTRA_SECTION_TITLE,
                sectionTitle
        );

        startActivity(intent);
    }

    private String getQuestionNumberText(Question question) {
        String number = question.getOriginalNumber();

        if (number == null || number.trim().isEmpty()) {
            number = String.valueOf(question.getSortOrder());
        }

        switch (getCurrentLanguage()) {
            case "hy":
                return "Հարց " + number;

            case "en":
                return "Question " + number;

            default:
                return "Вопрос № " + number;
        }
    }

    private String getSourcePageText(int page) {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Էջ " + page;

            case "en":
                return "Page " + page;

            default:
                return "Страница " + page;
        }
    }

    private String getQuestionsCountText(int count) {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Հարցերի քանակը՝ " + count;

            case "en":
                return "Questions: " + count;

            default:
                return "Количество вопросов: " + count;
        }
    }

    private String getCurrentLanguage() {
        return getResources()
                .getConfiguration()
                .locale
                .getLanguage();
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {
        if (questionRepository != null) {
            questionRepository.close();
        }

        super.onDestroy();
    }
}