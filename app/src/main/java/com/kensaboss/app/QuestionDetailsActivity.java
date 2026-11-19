package com.kensaboss.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.AnswerOptionRepository;
import com.kensaboss.app.data.repository.CorrectAnswerRepository;
import com.kensaboss.app.data.repository.QuestionRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.AnswerOption;
import com.kensaboss.app.models.CorrectAnswer;
import com.kensaboss.app.models.Question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_QUESTION_ID = "question_id";

    public static final String EXTRA_BOOK_TITLE = "book_title";
    public static final String EXTRA_PART_TITLE = "part_title";
    public static final String EXTRA_CHAPTER_TITLE = "chapter_title";
    public static final String EXTRA_SECTION_TITLE = "section_title";

    private android.widget.EditText valueInput;
    private long questionId;

    private String bookTitle;
    private String partTitle;
    private String chapterTitle;
    private String sectionTitle;

    private Question currentQuestion;

    private TextView tvQuestionNumber;
    private TextView tvQuestionText;
    private TextView tvSourcePage;
    private TextView tvSelectedAnswer;
    private TextView tvCheckResult;
    private TextView tvNoOptions;

    private LinearLayout optionsContainer;

    private Button btnCheckAnswer;

    private final Set<Long> selectedOptionIds = new HashSet<>();

    private List<AnswerOption> answerOptions = new ArrayList<>();

    private QuestionRepository questionRepository;
    private AnswerOptionRepository answerOptionRepository;
    private CorrectAnswerRepository correctAnswerRepository;

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
        setContentView(R.layout.activity_question_details);

        questionId = getIntent().getLongExtra(EXTRA_QUESTION_ID, -1);

        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);
        partTitle = getIntent().getStringExtra(EXTRA_PART_TITLE);
        chapterTitle = getIntent().getStringExtra(EXTRA_CHAPTER_TITLE);
        sectionTitle = getIntent().getStringExtra(EXTRA_SECTION_TITLE);

        if (questionId <= 0) {
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();

        questionRepository = new QuestionRepository(this);
        answerOptionRepository = new AnswerOptionRepository(this);
        correctAnswerRepository = new CorrectAnswerRepository(this);

        loadQuestion();
    }

    private void initializeViews() {
        TextView tvBookTitle = findViewById(R.id.tvQuestionBookTitle);
        TextView tvPartTitle = findViewById(R.id.tvQuestionPartTitle);
        TextView tvChapterTitle = findViewById(R.id.tvQuestionChapterTitle);
        TextView tvSectionTitle = findViewById(R.id.tvQuestionSectionTitle);

        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvSourcePage = findViewById(R.id.tvSourcePage);

        tvSelectedAnswer = findViewById(R.id.tvSelectedAnswer);
        tvCheckResult = findViewById(R.id.tvCheckResult);
        tvNoOptions = findViewById(R.id.tvNoOptions);

        optionsContainer = findViewById(R.id.optionsContainer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);

        tvBookTitle.setText(safeText(bookTitle));
        tvPartTitle.setText(safeText(partTitle));
        tvChapterTitle.setText(safeText(chapterTitle));
        tvSectionTitle.setText(safeText(sectionTitle));
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnQuestionDetailsBack);

        btnBack.setOnClickListener(view -> finish());

        btnCheckAnswer.setOnClickListener(view -> checkAnswer());
    }

    private void loadQuestion() {
        currentQuestion = questionRepository.getQuestionById(questionId);

        if (currentQuestion == null) {
            tvQuestionNumber.setText(
                    localized(
                            "Вопрос не найден",
                            "Question not found",
                            "Հարցը չի գտնվել"
                    )
            );

            tvQuestionText.setVisibility(View.GONE);
            btnCheckAnswer.setVisibility(View.GONE);

            return;
        }

        String number = currentQuestion.getOriginalNumber();

        if (number == null || number.trim().isEmpty()) {
            number = String.valueOf(currentQuestion.getSortOrder());
        }

        tvQuestionNumber.setText(questionNumber(number));
        tvQuestionText.setText(
                safeText(currentQuestion.getQuestionText())
        );

        Integer sourcePage = currentQuestion.getSourcePage();

        if (sourcePage != null) {
            tvSourcePage.setText(sourcePageText(sourcePage));
            tvSourcePage.setVisibility(View.VISIBLE);
        } else {
            tvSourcePage.setVisibility(View.GONE);
        }

        answerOptions =
                answerOptionRepository.getAnswerOptionsByQuestionId(questionId);

        displayOptions();
        if (TypedAnswerGrader.supports(currentQuestion)) {
            tvNoOptions.setVisibility(View.GONE);
            valueInput=new android.widget.EditText(this);
            valueInput.setHint(getString(R.string.numeric_answer_hint));
            valueInput.setTextColor(getColor(R.color.kb_text_primary));
            optionsContainer.addView(valueInput);
            btnCheckAnswer.setText(getString(R.string.check_answer_action));
            btnCheckAnswer.setVisibility(View.VISIBLE);
        }
        StudyContent.tools(this, optionsContainer, questionId, currentQuestion.getSourcePage() == null ? 1 : currentQuestion.getSourcePage());
        Button explanation = new Button(this); explanation.setText(getString(R.string.explanation_source_action));
        explanation.setOnClickListener(v -> StudyContent.showExplanation(this,questionId));
        ((LinearLayout) tvCheckResult.getParent()).addView(explanation);
    }

    private void displayOptions() {
        optionsContainer.removeAllViews();
        selectedOptionIds.clear();

        tvSelectedAnswer.setVisibility(View.GONE);
        tvCheckResult.setVisibility(View.GONE);

        if (answerOptions == null || answerOptions.isEmpty()) {
            tvNoOptions.setVisibility(View.VISIBLE);
            btnCheckAnswer.setVisibility(View.GONE);
            return;
        }

        tvNoOptions.setVisibility(View.GONE);

        String questionType = currentQuestion.getQuestionType();

        if (Question.TYPE_SINGLE_CHOICE.equals(questionType)
                || Question.TYPE_TRUE_FALSE.equals(questionType)) {

            displaySingleChoiceOptions();

        } else if (Question.TYPE_MULTIPLE_CHOICE.equals(questionType)) {

            displayMultipleChoiceOptions();

        } else {
            displayReadOnlyOptions();
        }
    }

    private void displaySingleChoiceOptions() {
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.VERTICAL);

        for (AnswerOption option : answerOptions) {
            RadioButton radioButton = new RadioButton(this);

            radioButton.setId(View.generateViewId());
            radioButton.setText(getOptionDisplayText(option));
            radioButton.setTextSize(16);
            radioButton.setTextColor(getColor(R.color.kb_text_primary));
            radioButton.setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
            );

            radioButton.setTag(option.getId());

            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedOptionIds.clear();

            if (checkedId != -1) {
                RadioButton selected = group.findViewById(checkedId);

                if (selected != null) {
                    Long optionId = (Long) selected.getTag();

                    selectedOptionIds.add(optionId);

                    showSelectedAnswer();
                    resetCheckResult();
                }
            }
        });

        optionsContainer.addView(radioGroup);
        btnCheckAnswer.setVisibility(View.VISIBLE);
    }

    private void displayMultipleChoiceOptions() {
        for (AnswerOption option : answerOptions) {
            CheckBox checkBox = new CheckBox(this);

            checkBox.setText(getOptionDisplayText(option));
            checkBox.setTextSize(16);
            checkBox.setTextColor(getColor(R.color.kb_text_primary));
            checkBox.setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
            );

            checkBox.setTag(option.getId());

            checkBox.setOnCheckedChangeListener((button, checked) -> {
                Long optionId = (Long) button.getTag();

                if (checked) {
                    selectedOptionIds.add(optionId);
                } else {
                    selectedOptionIds.remove(optionId);
                }

                showSelectedAnswer();
                resetCheckResult();
            });

            optionsContainer.addView(checkBox);
        }

        btnCheckAnswer.setVisibility(View.VISIBLE);
    }

    private void displayReadOnlyOptions() {
        for (AnswerOption option : answerOptions) {
            TextView textView = new TextView(this);

            textView.setText(getOptionDisplayText(option));
            textView.setTextSize(16);
            textView.setTextColor(getColor(R.color.kb_text_primary));
            textView.setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
            );

            optionsContainer.addView(textView);
        }

        btnCheckAnswer.setVisibility(View.GONE);

        tvCheckResult.setText(
                localized(
                        "Для этого типа вопроса проверка будет добавлена отдельно.",
                        "Checking for this question type will be added separately.",
                        "Այս տեսակի հարցի ստուգումը կավելացվի առանձին։"
                )
        );

        tvCheckResult.setVisibility(View.VISIBLE);
    }

    private void checkAnswer() {
        if (valueInput != null) {
            if(valueInput.getText().toString().trim().isEmpty())return;
            Boolean correct=TypedAnswerGrader.evaluate(currentQuestion,valueInput.getText().toString(),correctAnswerRepository.getCorrectAnswersByQuestionId(questionId));
            tvCheckResult.setText(correct==null?"Պատասխանը գնահատված չէ։":correct?"Ճիշտ է։":"Սխալ է։ Բացիր բացատրությունն ու տպագիր բանալին։");
            tvCheckResult.setVisibility(View.VISIBLE);
            return;
        }
        if (selectedOptionIds.isEmpty()) {
            tvCheckResult.setText(
                    localized(
                            "Сначала выбери ответ.",
                            "Select an answer first.",
                            "Նախ ընտրիր պատասխանը։"
                    )
            );

            tvCheckResult.setVisibility(View.VISIBLE);
            return;
        }

        String questionType = currentQuestion.getQuestionType();

        if (!Question.TYPE_SINGLE_CHOICE.equals(questionType)
                && !Question.TYPE_TRUE_FALSE.equals(questionType)
                && !Question.TYPE_MULTIPLE_CHOICE.equals(questionType)) {

            showCannotCheckMessage(
                    localized(
                            "Для этого типа вопроса проверка пока не поддерживается.",
                            "Checking is not supported for this question type yet.",
                            "Այս տեսակի հարցի ստուգումը դեռ աջակցվում չէ։"
                    )
            );

            return;
        }

        List<CorrectAnswer> correctAnswers =
                correctAnswerRepository.getCorrectAnswersByQuestionId(
                        questionId
                );

        String verificationProblem =
                getVerificationProblem(correctAnswers);

        if (verificationProblem != null) {
            showCannotCheckMessage(verificationProblem);
            return;
        }

        Set<Long> correctOptionIds = new HashSet<>();

        for (CorrectAnswer answer : correctAnswers) {
            if (!CorrectAnswer.TYPE_OPTION.equals(answer.getAnswerType())
                    || answer.getOptionId() == null) {

                showCannotCheckMessage(
                        localized(
                                "Тип правильного ответа не соответствует вариантам вопроса.",
                                "The correct answer type does not match the question options.",
                                "Ճիշտ պատասխանի տեսակը չի համապատասխանում հարցի տարբերակներին։"
                        )
                );

                return;
            }

            correctOptionIds.add(answer.getOptionId());
        }

        if (correctOptionIds.isEmpty()) {
            showCannotCheckMessage(
                    localized(
                            "В базе нет подтверждённого правильного ответа.",
                            "There is no verified correct answer in the database.",
                            "Տվյալների բազայում հաստատված ճիշտ պատասխան չկա։"
                    )
            );

            return;
        }

        boolean isCorrect =
                selectedOptionIds.equals(correctOptionIds);

        if (isCorrect) {
            showCorrectResult();
        } else {
            showIncorrectResult(correctOptionIds);
        }

        btnCheckAnswer.setEnabled(false);
    }

    private String getVerificationProblem(
            List<CorrectAnswer> correctAnswers
    ) {
        String questionStatus = currentQuestion.getSourceKeyStatus();

        if (questionStatus == null || questionStatus.trim().isEmpty()) {
            return localized(
                    "Статус ключа этого вопроса не указан. Проверка невозможна.",
                    "The verification status of this question is missing. Checking is unavailable.",
                    "Այս հարցի պատասխանի ստուգման կարգավիճակը նշված չէ։"
            );
        }

        if (!Question.KEY_VERIFIED.equals(questionStatus)) {
            return getStatusMessage(questionStatus);
        }

        if (correctAnswers == null || correctAnswers.isEmpty()) {
            return localized(
                    "Правильный ответ отсутствует в базе данных.",
                    "The correct answer is missing from the database.",
                    "Տվյալների բազայում ճիշտ պատասխանը բացակայում է։"
            );
        }

        for (CorrectAnswer answer : correctAnswers) {
            if (!CorrectAnswer.STATUS_VERIFIED.equals(
                    answer.getVerificationStatus()
            )) {
                return getStatusMessage(
                        answer.getVerificationStatus()
                );
            }
        }

        return null;
    }

    private String getStatusMessage(String status) {
        if (Question.KEY_MISSING.equals(status)) {
            return localized(
                    "В учебнике или базе отсутствует ключ правильного ответа. Оценка не выставлена.",
                    "The answer key is missing. No score was assigned.",
                    "Ճիշտ պատասխանի բանալին բացակայում է։ Գնահատական չի տրվել։"
            );
        }

        if (Question.KEY_AMBIGUOUS.equals(status)
                || CorrectAnswer.STATUS_AMBIGUOUS.equals(status)) {
            return localized(
                    "Правильный ответ неоднозначен. Необходимо дополнительное уточнение.",
                    "The correct answer is ambiguous and needs clarification.",
                    "Ճիշտ պատասխանը միանշանակ չէ և լրացուցիչ ճշտման կարիք ունի։"
            );
        }

        if (Question.KEY_CONFLICTING.equals(status)
                || CorrectAnswer.STATUS_CONFLICTING.equals(status)) {
            return localized(
                    "В источниках обнаружено противоречие. Проверка отключена.",
                    "Conflicting answers were found in the sources. Checking is disabled.",
                    "Աղբյուրներում հակասություն է հայտնաբերվել։ Ստուգումն անջատված է։"
            );
        }

        if (Question.KEY_PRINTING_ERROR.equals(status)
                || CorrectAnswer.STATUS_PRINTING_ERROR.equals(status)) {
            return localized(
                    "Возможна опечатка в учебнике. Ответ требует проверки.",
                    "There may be a printing error in the textbook. The answer needs review.",
                    "Հնարավոր է՝ դասագրքում տպագրական սխալ կա։ Պատասխանը պետք է ստուգվի։"
            );
        }

        return localized(
                "Правильный ответ ещё не подтверждён. Оценка не выставлена.",
                "The correct answer has not been verified. No score was assigned.",
                "Ճիշտ պատասխանը դեռ հաստատված չէ։ Գնահատական չի տրվել։"
        );
    }

    private void showCorrectResult() {
        tvCheckResult.setText(
                localized(
                        "Правильно! Ответ совпадает с подтверждённым ключом.",
                        "Correct! Your answer matches the verified key.",
                        "Ճիշտ է։ Պատասխանը համապատասխանում է հաստատված բանալուն։"
                )
        );

        tvCheckResult.setTextColor(
                getColor(R.color.kb_primary)
        );

        tvCheckResult.setVisibility(View.VISIBLE);
    }

    private void showIncorrectResult(Set<Long> correctOptionIds) {
        StringBuilder result = new StringBuilder();

        result.append(
                localized(
                        "Ответ неправильный.\nПравильный ответ: ",
                        "Incorrect answer.\nCorrect answer: ",
                        "Սխալ պատասխան։\nՃիշտ պատասխան՝ "
                )
        );

        boolean first = true;

        for (AnswerOption option : answerOptions) {
            if (correctOptionIds.contains(option.getId())) {
                if (!first) {
                    result.append("\n");
                }

                result.append(getOptionDisplayText(option));
                first = false;
            }
        }

        tvCheckResult.setText(result.toString());

        tvCheckResult.setTextColor(
                getColor(R.color.kb_text_primary)
        );

        tvCheckResult.setVisibility(View.VISIBLE);
    }

    private void showCannotCheckMessage(String message) {
        tvCheckResult.setText(message);
        tvCheckResult.setTextColor(
                getColor(R.color.kb_text_secondary)
        );
        tvCheckResult.setVisibility(View.VISIBLE);
    }

    private void showSelectedAnswer() {
        if (selectedOptionIds.isEmpty()) {
            tvSelectedAnswer.setVisibility(View.GONE);
            return;
        }

        StringBuilder selectedText = new StringBuilder();

        for (AnswerOption option : answerOptions) {
            if (selectedOptionIds.contains(option.getId())) {
                if (selectedText.length() > 0) {
                    selectedText.append("\n");
                }

                selectedText.append(getOptionDisplayText(option));
            }
        }

        tvSelectedAnswer.setText(
                localized(
                        "Выбрано:\n",
                        "Selected:\n",
                        "Ընտրված է՝\n"
                ) + selectedText
        );

        tvSelectedAnswer.setVisibility(View.VISIBLE);
    }

    private void resetCheckResult() {
        tvCheckResult.setVisibility(View.GONE);
        btnCheckAnswer.setEnabled(true);
    }

    private String getOptionDisplayText(AnswerOption option) {
        String label = option.getOptionLabel();

        if (label == null || label.trim().isEmpty()) {
            label = String.valueOf(option.getSortOrder() + 1);
        }

        return label + ". " + safeText(option.getOptionText());
    }

    private String questionNumber(String number) {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Հարց " + number;

            case "en":
                return "Question " + number;

            default:
                return "Вопрос № " + number;
        }
    }

    private String sourcePageText(int page) {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Էջ " + page;

            case "en":
                return "Page " + page;

            default:
                return "Страница " + page;
        }
    }

    private String localized(
            String russian,
            String english,
            String armenian
    ) {
        switch (getCurrentLanguage()) {
            case "hy":
                return armenian;

            case "en":
                return english;

            default:
                return russian;
        }
    }

    private String getCurrentLanguage() {
        return getResources()
                .getConfiguration()
                .locale
                .getLanguage();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
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

        if (answerOptionRepository != null) {
            answerOptionRepository.close();
        }

        if (correctAnswerRepository != null) {
            correctAnswerRepository.close();
        }

        super.onDestroy();
    }
}