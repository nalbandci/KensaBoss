
package com.kensaboss.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.AnswerOptionRepository;
import com.kensaboss.app.data.repository.BookRepository;
import com.kensaboss.app.data.repository.CorrectAnswerRepository;
import com.kensaboss.app.data.repository.ExplanationRepository;
import com.kensaboss.app.data.repository.QuestionRepository;
import com.kensaboss.app.data.repository.SessionAnswerRepository;
import com.kensaboss.app.data.repository.TrainingSessionRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.AnswerOption;
import com.kensaboss.app.models.CorrectAnswer;
import com.kensaboss.app.models.Explanation;
import com.kensaboss.app.models.Question;
import com.kensaboss.app.models.SessionAnswer;
import com.kensaboss.app.models.TrainingSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TrainingQuestionActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "session_id";

    private android.widget.EditText typedInput;
    private long sessionId;

    private TrainingSession currentSession;

    private List<Long> questionIds = new ArrayList<>();

    private int currentQuestionIndex = 0;

    private Question currentQuestion;

    private String bookTitle = "";

    private TextView tvProgress;
    private TextView tvBookTitle;
    private TextView tvQuestionType;
    private TextView tvQuestionText;
    private TextView tvInstruction;

    private TextView tvFeedbackTitle;
    private TextView tvFeedbackDetails;

    private ProgressBar progressBar;

    private LinearLayout optionsContainer;
    private LinearLayout feedbackContainer;

    private ImageButton btnBack;
    private ImageButton btnMenu;

    private Button btnCheck;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnFinish;

    private final Set<Long> selectedOptionIds = new HashSet<>();

    private List<AnswerOption> answerOptions = new ArrayList<>();

    private boolean answerLocked = false;

    private QuestionRepository questionRepository;
    private AnswerOptionRepository answerOptionRepository;
    private CorrectAnswerRepository correctAnswerRepository;
    private SessionAnswerRepository sessionAnswerRepository;
    private TrainingSessionRepository trainingSessionRepository;
    private ExplanationRepository explanationRepository;
    private BookRepository bookRepository;

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

        setContentView(R.layout.activity_training_question);

        sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1);

        if (sessionId <= 0) {
            showErrorAndClose(
                    localized(
                            "Не удалось определить тренировку.",
                            "Training session could not be identified.",
                            "Հնարավոր չեղավ որոշել ուսումնական վարժանքը։"
                    )
            );
            return;
        }

        initializeViews();

        initializeRepositories();

        setupClickListeners();

        loadTrainingSession();
    }

    @Override
    protected void onPause() {
        if (currentSession != null && currentQuestion != null && TrainingSession.STATUS_IN_PROGRESS.equals(currentSession.getStatus())) {
            saveBeforeNavigation();
        }
        super.onPause();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnTrainingQuestionBack);
        btnMenu = findViewById(R.id.btnTrainingQuestionMenu);

        tvProgress = findViewById(R.id.tvTrainingQuestionProgress);
        progressBar = findViewById(R.id.pbTrainingQuestionProgress);

        tvBookTitle = findViewById(R.id.tvTrainingQuestionBookTitle);
        tvQuestionType = findViewById(R.id.tvTrainingQuestionType);
        tvQuestionText = findViewById(R.id.tvTrainingQuestionText);
        tvInstruction = findViewById(R.id.tvTrainingQuestionInstruction);

        optionsContainer = findViewById(
                R.id.trainingQuestionOptionsContainer
        );

        feedbackContainer = findViewById(
                R.id.trainingQuestionFeedbackContainer
        );

        tvFeedbackTitle = findViewById(
                R.id.tvTrainingQuestionFeedbackTitle
        );

        tvFeedbackDetails = findViewById(
                R.id.tvTrainingQuestionFeedbackDetails
        );

        btnCheck = findViewById(R.id.btnTrainingQuestionCheck);
        btnPrevious = findViewById(R.id.btnTrainingQuestionPrevious);
        btnNext = findViewById(R.id.btnTrainingQuestionNext);
        btnFinish = findViewById(R.id.btnTrainingQuestionFinish);
    }

    private void initializeRepositories() {
        questionRepository = new QuestionRepository(this);

        answerOptionRepository = new AnswerOptionRepository(this);

        correctAnswerRepository = new CorrectAnswerRepository(this);

        sessionAnswerRepository = new SessionAnswerRepository(this);

        trainingSessionRepository = new TrainingSessionRepository(this);

        explanationRepository = new ExplanationRepository(this);

        bookRepository = new BookRepository(this);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(view -> confirmExit());

        btnMenu.setOnClickListener(this::showMenu);

        btnCheck.setOnClickListener(view -> checkCurrentAnswer());

        btnPrevious.setOnClickListener(view -> showPreviousQuestion());

        btnNext.setOnClickListener(view -> showNextQuestion());

        btnFinish.setOnClickListener(view -> confirmFinish());
    }

    // ---------------------------------------------------------
    // Загрузка тренировки
    // ---------------------------------------------------------

    private void loadTrainingSession() {
        currentSession = trainingSessionRepository.getSessionById(sessionId);

        if (currentSession == null) {
            showErrorAndClose(
                    localized(
                            "Тренировка не найдена.",
                            "Training session was not found.",
                            "Ուսումնական վարժանքը չի գտնվել։"
                    )
            );
            return;
        }

        if (!TrainingSession.STATUS_IN_PROGRESS.equals(
                currentSession.getStatus()
        )) {
            showErrorAndClose(
                    localized(
                            "Эта тренировка уже завершена или отменена.",
                            "This training session has already been completed or cancelled.",
                            "Այս ուսումնական վարժանքն արդեն ավարտված է կամ չեղարկված։"
                    )
            );
            return;
        }

        questionIds = trainingSessionRepository.getQuestionIdsForSession(
                sessionId
        );

        if (questionIds == null || questionIds.isEmpty()) {
            showErrorAndClose(
                    localized(
                            "В тренировке нет вопросов.",
                            "There are no questions in this training session.",
                            "Այս ուսումնական վարժանքում հարցեր չկան։"
                    )
            );
            return;
        }

        Question firstQuestion = questionRepository.getQuestionById(
                questionIds.get(0)
        );

        if (firstQuestion == null) {
            showErrorAndClose(
                    localized(
                            "Не удалось загрузить вопросы тренировки.",
                            "Could not load the training questions.",
                            "Չհաջողվեց բեռնել վարժանքի հարցերը։"
                    )
            );
            return;
        }

        loadBookTitle(firstQuestion.getBookId());

        int savedIndex = getPreferences(MODE_PRIVATE).getInt("position_" + sessionId, 0);
        showQuestion(Math.max(0, Math.min(savedIndex, questionIds.size() - 1)));
    }

    private void loadBookTitle(long bookId) {
        if (bookId <= 0) {
            bookTitle = "";
            return;
        }

        com.kensaboss.app.models.Book book =
                bookRepository.getBookById(bookId);

        if (book != null && book.getTitle() != null) {
            bookTitle = book.getTitle();
        }
    }

    // ---------------------------------------------------------
    // Отображение вопроса
    // ---------------------------------------------------------

    private void showQuestion(int index) {
        if (index < 0 || index >= questionIds.size()) {
            return;
        }

        currentQuestionIndex = index;
        getPreferences(MODE_PRIVATE).edit().putInt("position_" + sessionId, index).apply();

        long questionId = questionIds.get(index);

        currentQuestion = questionRepository.getQuestionById(questionId);

        if (currentQuestion == null) {
            Toast.makeText(
                    this,
                    localized(
                            "Вопрос не найден.",
                            "Question not found.",
                            "Հարցը չի գտնվել։"
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        selectedOptionIds.clear();
        typedInput = null;

        answerLocked = false;

        answerOptions = answerOptionRepository
                .getAnswerOptionsByQuestionId(questionId);

        tvBookTitle.setText(bookTitle);

        tvQuestionText.setText(
                safeText(currentQuestion.getQuestionText())
        );

        updateProgress();

        updateQuestionType();

        feedbackContainer.setVisibility(View.GONE);

        optionsContainer.removeAllViews();

        displayQuestionOptions();

        restoreSavedAnswer();

        updateNavigationButtons();
    }

    private void updateProgress() {
        int current = currentQuestionIndex + 1;

        int total = questionIds.size();

        tvProgress.setText(
                localized(
                        "Вопрос " + current + " из " + total,
                        "Question " + current + " of " + total,
                        "Հարց " + current + " / " + total
                )
        );

        int percentage = (current * 100) / total;

        progressBar.setProgress(percentage);
    }

    private void updateQuestionType() {
        String type = currentQuestion.getQuestionType();

        if (Question.TYPE_SINGLE_CHOICE.equals(type)) {
            tvQuestionType.setText(
                    localized(
                            "Один правильный ответ",
                            "Single choice",
                            "Մեկ ճիշտ պատասխան"
                    )
            );

            tvInstruction.setText(
                    localized(
                            "Выбери один вариант ответа.",
                            "Select one answer.",
                            "Ընտրիր մեկ պատասխան։"
                    )
            );

        } else if (Question.TYPE_MULTIPLE_CHOICE.equals(type)) {
            tvQuestionType.setText(
                    localized(
                            "Несколько правильных ответов",
                            "Multiple choice",
                            "Մի քանի ճիշտ պատասխան"
                    )
            );

            tvInstruction.setText(
                    localized(
                            "Можно выбрать несколько вариантов.",
                            "You can select multiple answers.",
                            "Կարող ես ընտրել մի քանի պատասխան։"
                    )
            );

        } else if (Question.TYPE_TRUE_FALSE.equals(type)) {
            tvQuestionType.setText(
                    localized(
                            "Верно или неверно",
                            "True or false",
                            "Ճիշտ կամ սխալ"
                    )
            );

            tvInstruction.setText(
                    localized(
                            "Выбери один вариант ответа.",
                            "Select one answer.",
                            "Ընտրիր մեկ պատասխան։"
                    )
            );

        } else {
            tvQuestionType.setText(
                    localized(
                            "Тип вопроса",
                            "Question type",
                            "Հարցի տեսակ"
                    )
            );

            tvInstruction.setText(
                    localized(
                            "Этот тип вопроса пока не поддерживается в тренировке.",
                            "This question type is not supported in training yet.",
                            "Այս տեսակի հարցը դեռ չի աջակցվում վարժանքում։"
                    )
            );
        }
    }

    // ---------------------------------------------------------
    // Отображение вариантов
    // ---------------------------------------------------------

    private void displayQuestionOptions() {
        optionsContainer.removeAllViews();
        StudyContent.tools(this, optionsContainer, currentQuestion.getId(), currentQuestion.getSourcePage() == null ? 1 : currentQuestion.getSourcePage());
        if (isTyped(currentQuestion)) {
            String previous = typedInput == null ? "" : typedInput.getText().toString();
            typedInput = new android.widget.EditText(this);
            typedInput.setText(previous);
            typedInput.setTextColor(getColor(R.color.kb_text_primary));
            typedInput.setHint(localized("Введи ответ", "Enter answer", "Մուտքագրիր պատասխանը"));
            typedInput.setEnabled(!answerLocked);
            optionsContainer.addView(typedInput);
            tvQuestionType.setText(Question.TYPE_MATCHING.equals(currentQuestion.getQuestionType()) ? "Համապատասխանեցում" : Question.TYPE_SEQUENCE.equals(currentQuestion.getQuestionType()) ? "Հաջորդականություն" : Question.TYPE_NUMERIC.equals(currentQuestion.getQuestionType()) ? "Թվային պատասխան" : "Տեքստային պատասխան");
            tvInstruction.setText(localized("Введи ответ в поле ниже.", "Enter your answer below.", "Մուտքագրիր պատասխանը ներքևում։"));
            if (Question.TYPE_MATCHING.equals(currentQuestion.getQuestionType()) || Question.TYPE_SEQUENCE.equals(currentQuestion.getQuestionType())) tvInstruction.setText("Մուտքագրիր թվերը հերթով՝ ստորակետերով բաժանված, օրինակ՝ 2,1,3։ Համապատասխանեցման դեպքում՝ A, B, C… կարգով։");
            updateCheckButtonVisibility();
            return;
        }
        if (answerOptions == null || answerOptions.isEmpty()) {
            TextView emptyView = new TextView(this);

            emptyView.setText(
                    localized(
                            "У этого вопроса нет вариантов ответа.",
                            "This question has no answer options.",
                            "Այս հարցը պատասխանների տարբերակներ չունի։"
                    )
            );

            emptyView.setTextSize(15);

            emptyView.setTextColor(
                    getColor(R.color.kb_text_secondary)
            );

            optionsContainer.addView(emptyView);

            updateCheckButtonVisibility();

            return;
        }

        String type = currentQuestion.getQuestionType();

        if (Question.TYPE_SINGLE_CHOICE.equals(type)
                || Question.TYPE_TRUE_FALSE.equals(type)) {

            displaySingleChoiceOptions();

        } else if (Question.TYPE_MULTIPLE_CHOICE.equals(type)) {

            displayMultipleChoiceOptions();

        } else {
            displayReadOnlyOptions();
        }

        updateCheckButtonVisibility();
    }

    private void displaySingleChoiceOptions() {
        RadioGroup radioGroup = new RadioGroup(this);

        radioGroup.setOrientation(LinearLayout.VERTICAL);

        for (AnswerOption option : answerOptions) {
            RadioButton radioButton = new RadioButton(this);

            radioButton.setId(View.generateViewId());

            radioButton.setText(getOptionDisplayText(option));

            radioButton.setTextSize(16);

            radioButton.setTextColor(
                    getColor(R.color.kb_text_primary)
            );

            radioButton.setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
            );

            radioButton.setTag(option.getId());

            radioButton.setEnabled(!answerLocked);

            radioGroup.addView(radioButton);

            if (selectedOptionIds.contains(option.getId())) {
                radioButton.setChecked(true);
            }
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (answerLocked) {
                return;
            }

            selectedOptionIds.clear();

            if (checkedId != -1) {
                RadioButton selected = group.findViewById(checkedId);

                if (selected != null) {
                    Long optionId = (Long) selected.getTag();

                    selectedOptionIds.add(optionId);
                }
            }

            feedbackContainer.setVisibility(View.GONE);
        });

        optionsContainer.addView(radioGroup);
    }

    private void displayMultipleChoiceOptions() {
        for (AnswerOption option : answerOptions) {
            CheckBox checkBox = new CheckBox(this);

            checkBox.setText(getOptionDisplayText(option));

            checkBox.setTextSize(16);

            checkBox.setTextColor(
                    getColor(R.color.kb_text_primary)
            );

            checkBox.setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
            );

            checkBox.setTag(option.getId());

            checkBox.setEnabled(!answerLocked);

            checkBox.setChecked(
                    selectedOptionIds.contains(option.getId())
            );

            checkBox.setOnCheckedChangeListener((button, checked) -> {
                if (answerLocked) {
                    return;
                }

                Long optionId = (Long) button.getTag();

                if (checked) {
                    selectedOptionIds.add(optionId);
                } else {
                    selectedOptionIds.remove(optionId);
                }

                feedbackContainer.setVisibility(View.GONE);
            });

            optionsContainer.addView(checkBox);
        }
    }

    private void displayReadOnlyOptions() {
        for (AnswerOption option : answerOptions) {
            TextView textView = new TextView(this);

            textView.setText(getOptionDisplayText(option));

            textView.setTextSize(16);

            textView.setTextColor(
                    getColor(R.color.kb_text_primary)
            );

            textView.setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
            );

            optionsContainer.addView(textView);
        }
    }

    // ---------------------------------------------------------
    // Управление кнопкой проверки
    // ---------------------------------------------------------

    private void updateCheckButtonVisibility() {
        boolean isImmediateMode =
                currentSession != null
                        && TrainingSession.FEEDBACK_IMMEDIATE.equals(
                        currentSession.getFeedbackMode()
                );

        boolean isSupportedType = isSupportedQuestionType();

        boolean hasOptions =
                answerOptions != null && !answerOptions.isEmpty();

        boolean shouldShow =
                isImmediateMode && ((isSupportedType && hasOptions) || isTyped(currentQuestion));

        btnCheck.setVisibility(
                shouldShow ? View.VISIBLE : View.GONE
        );

        btnCheck.setEnabled(
                shouldShow && !answerLocked
        );
    }

    // ---------------------------------------------------------
    // Восстановление сохранённого ответа
    // ---------------------------------------------------------

    private void restoreSavedAnswer() {
        SessionAnswer savedAnswer =
                sessionAnswerRepository.getAnswerByAttempt(
                        sessionId,
                        currentQuestion.getId(),
                        1
                );

        if (savedAnswer == null) {
            return;
        }

        if (isTyped(currentQuestion) && typedInput != null) typedInput.setText(savedAnswer.getSelectedAnswer());
        else parseSelectedAnswer(savedAnswer.getSelectedAnswer());

        String feedbackMode = currentSession.getFeedbackMode();

        if (TrainingSession.FEEDBACK_IMMEDIATE.equals(feedbackMode)
                && savedAnswer.getIsCorrect() != null) {

            answerLocked = true;

            displaySavedImmediateFeedback(
                    savedAnswer.getIsCorrect()
            );
        }

        displayQuestionOptions();
    }

    private void parseSelectedAnswer(String value) {
        selectedOptionIds.clear();

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String[] ids = value.split(",");

        for (String id : ids) {
            try {
                long optionId = Long.parseLong(id.trim());

                if (optionId > 0) {
                    selectedOptionIds.add(optionId);
                }

            } catch (NumberFormatException ignored) {
                // Повреждённый идентификатор пропускается.
            }
        }
    }

    // ---------------------------------------------------------
    // Проверка ответа
    // ---------------------------------------------------------

    private boolean isTyped(Question question) {
        return question != null && (Question.TYPE_NUMERIC.equals(question.getQuestionType()) || Question.TYPE_TEXT.equals(question.getQuestionType()) || Question.TYPE_MATCHING.equals(question.getQuestionType()) || Question.TYPE_SEQUENCE.equals(question.getQuestionType()));
    }
    private Boolean gradeTyped(Question question, String value) { return TypedAnswerGrader.evaluate(question,value,correctAnswerRepository.getCorrectAnswersByQuestionId(question.getId())); }
    private void checkCurrentAnswer() {
        if (isTyped(currentQuestion)) {
            if (typedInput == null || typedInput.getText().toString().trim().isEmpty()) return;
            Boolean correct = gradeTyped(currentQuestion, typedInput.getText().toString());
            saveCurrentAnswer(correct);
            answerLocked = correct != null;
            typedInput.setEnabled(!answerLocked);
            StringBuilder details = new StringBuilder();
            for (CorrectAnswer key : correctAnswerRepository.getCorrectAnswersByQuestionId(currentQuestion.getId()))
                if (CorrectAnswer.STATUS_VERIFIED.equals(key.getVerificationStatus())) details.append(key.getAnswerValue()).append("\n");
            showFeedback(correct == null ? localized("Без оценки", "Ungraded", "Առանց գնահատականի") : correct ? localized("Правильно", "Correct", "Ճիշտ") : localized("Неправильно", "Incorrect", "Սխալ"), details.toString(), Boolean.TRUE.equals(correct));
            updateCheckButtonVisibility();
            return;
        }
        if (currentQuestion == null) {
            return;
        }

        // Защита: в отложенном режиме проверка до завершения запрещена.
        if (currentSession == null
                || !TrainingSession.FEEDBACK_IMMEDIATE.equals(
                currentSession.getFeedbackMode()
        )) {

            return;
        }

        if (selectedOptionIds.isEmpty()) {
            showFeedback(
                    localized(
                            "Сначала выбери ответ.",
                            "Select an answer first.",
                            "Նախ ընտրիր պատասխանը։"
                    ),
                    "",
                    false
            );

            return;
        }

        if (!isSupportedQuestionType()) {
            showFeedback(
                    localized(
                            "Проверка этого типа вопроса пока не поддерживается.",
                            "Checking this question type is not supported yet.",
                            "Այս տեսակի հարցի ստուգումը դեռ չի աջակցվում։"
                    ),
                    "",
                    false
            );

            return;
        }

        List<CorrectAnswer> correctAnswers =
                correctAnswerRepository.getCorrectAnswersByQuestionId(
                        currentQuestion.getId()
                );

        String verificationProblem =
                getVerificationProblem(correctAnswers);

        if (verificationProblem != null) {
            saveCurrentAnswer(null);

            showFeedback(
                    localized(
                            "Ответ сохранён без оценки.",
                            "Answer saved without a score.",
                            "Պատասխանը պահպանվել է առանց գնահատականի։"
                    ),
                    verificationProblem,
                    false
            );

            return;
        }

        Set<Long> correctOptionIds = new HashSet<>();

        for (CorrectAnswer answer : correctAnswers) {
            if (!CorrectAnswer.TYPE_OPTION.equals(
                    answer.getAnswerType()
            ) || answer.getOptionId() == null) {

                saveCurrentAnswer(null);

                showFeedback(
                        localized(
                                "Невозможно проверить ответ: тип ключа не соответствует вариантам вопроса.",
                                "Cannot check the answer: the answer key does not match the question options.",
                                "Հնարավոր չէ ստուգել պատասխանը․ պատասխանի բանալին չի համապատասխանում հարցի տարբերակներին։"
                        ),
                        "",
                        false
                );

                return;
            }

            correctOptionIds.add(answer.getOptionId());
        }

        if (correctOptionIds.isEmpty()) {
            saveCurrentAnswer(null);

            showFeedback(
                    localized(
                            "Подтверждённый правильный ответ отсутствует.",
                            "There is no verified correct answer.",
                            "Հաստատված ճիշտ պատասխան չկա։"
                    ),
                    "",
                    false
            );

            return;
        }

        boolean isCorrect = selectedOptionIds.equals(correctOptionIds);

        saveCurrentAnswer(isCorrect);

        answerLocked = true;

        disableAnswerOptions();

        btnCheck.setEnabled(false);

        displayResult(isCorrect, correctOptionIds);
    }

    private boolean isSupportedQuestionType() {
        String type = currentQuestion.getQuestionType();

        return Question.TYPE_SINGLE_CHOICE.equals(type)
                || Question.TYPE_MULTIPLE_CHOICE.equals(type)
                || Question.TYPE_TRUE_FALSE.equals(type);
    }

    private String getVerificationProblem(
            List<CorrectAnswer> correctAnswers
    ) {
        String questionStatus = currentQuestion.getSourceKeyStatus();

        if (!Question.KEY_VERIFIED.equals(questionStatus)) {
            return getStatusMessage(questionStatus);
        }

        if (correctAnswers == null || correctAnswers.isEmpty()) {
            return localized(
                    "В базе данных отсутствует правильный ответ.",
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
                    "Правильный ответ неоднозначен и требует уточнения. Оценка не выставлена.",
                    "The correct answer is ambiguous and needs clarification. No score was assigned.",
                    "Ճիշտ պատասխանը միանշանակ չէ և ճշտման կարիք ունի։ Գնահատական չի տրվել։"
            );
        }

        if (Question.KEY_CONFLICTING.equals(status)
                || CorrectAnswer.STATUS_CONFLICTING.equals(status)) {

            return localized(
                    "В источниках обнаружено противоречие. Оценка не выставлена.",
                    "Conflicting answers were found in the sources. No score was assigned.",
                    "Աղբյուրներում հակասություն է հայտնաբերվել։ Գնահատական չի տրվել։"
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

    // ---------------------------------------------------------
    // Результат и объяснение
    // ---------------------------------------------------------

    private void displayResult(
            boolean isCorrect,
            Set<Long> correctOptionIds
    ) {
        String title;
        StringBuilder details = new StringBuilder();

        if (isCorrect) {
            title = localized(
                    "Правильно!",
                    "Correct!",
                    "Ճիշտ է։"
            );

            details.append(
                    localized(
                            "Твой ответ совпадает с подтверждённым ключом.",
                            "Your answer matches the verified answer key.",
                            "Քո պատասխանը համապատասխանում է հաստատված բանալուն։"
                    )
            );

        } else {
            title = localized(
                    "Неправильно",
                    "Incorrect",
                    "Սխալ է։"
            );

            details.append(
                    localized(
                            "Правильный ответ:",
                            "Correct answer:",
                            "Ճիշտ պատասխան՝"
                    )
            );

            details.append("\n");

            details.append(
                    getOptionsText(correctOptionIds)
            );
        }

        appendExplanation(details);

        showFeedback(title, details.toString(), isCorrect);
    }

    private void displaySavedImmediateFeedback(boolean isCorrect) {
        if (isTyped(currentQuestion)) {
            StringBuilder details = new StringBuilder();
            for (CorrectAnswer key : correctAnswerRepository.getCorrectAnswersByQuestionId(currentQuestion.getId())) {
                if (CorrectAnswer.STATUS_VERIFIED.equals(key.getVerificationStatus())) details.append(key.getAnswerValue()).append("\n");
            }
            appendExplanation(details);
            showFeedback(localized(isCorrect ? "Правильно" : "Неправильно", isCorrect ? "Correct" : "Incorrect", isCorrect ? "Ճիշտ" : "Սխալ"), details.toString(), isCorrect);
            return;
        }
        Set<Long> correctOptionIds = getVerifiedCorrectOptionIds();

        if (correctOptionIds == null) {
            showFeedback(
                    localized(
                            "Ответ был сохранён, но правильный ключ больше не доступен для проверки.",
                            "The answer was saved, but the verified answer key is no longer available.",
                            "Պատասխանը պահպանվել է, սակայն հաստատված բանալին այլևս հասանելի չէ։"
                    ),
                    "",
                    false
            );

            return;
        }

        displayResult(isCorrect, correctOptionIds);
    }

    private Set<Long> getVerifiedCorrectOptionIds() {
        List<CorrectAnswer> correctAnswers =
                correctAnswerRepository.getCorrectAnswersByQuestionId(
                        currentQuestion.getId()
                );

        if (getVerificationProblem(correctAnswers) != null) {
            return null;
        }

        Set<Long> result = new HashSet<>();

        for (CorrectAnswer answer : correctAnswers) {
            if (!CorrectAnswer.TYPE_OPTION.equals(
                    answer.getAnswerType()
            ) || answer.getOptionId() == null) {
                return null;
            }

            result.add(answer.getOptionId());
        }

        return result.isEmpty() ? null : result;
    }

    private void appendExplanation(StringBuilder details) {
        Explanation explanation =
                explanationRepository.getExplanationByQuestionId(
                        currentQuestion.getId(),
                        getCurrentLanguage()
                );

        if (explanation == null) {
            return;
        }

        appendExplanationPart(
                details,
                localized(
                        "Объяснение:",
                        "Explanation:",
                        "Բացատրություն՝"
                ),
                explanation.getIntroduction()
        );

        appendExplanationPart(
                details,
                localized(
                        "Подробности:",
                        "Details:",
                        "Մանրամասներ՝"
                ),
                explanation.getDetailedText()
        );

        appendExplanationPart(
                details,
                localized(
                        "Этапы решения:",
                        "Solution steps:",
                        "Լուծման քայլերը՝"
                ),
                explanation.getSolutionSteps()
        );

        appendExplanationPart(
                details,
                localized(
                        "Дополнительно:",
                        "Additional notes:",
                        "Լրացուցիչ նշումներ՝"
                ),
                explanation.getAdditionalNotes()
        );
    }

    private void appendExplanationPart(
            StringBuilder builder,
            String heading,
            String content
    ) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append("\n\n");
        }

        builder.append(heading);
        builder.append("\n");
        builder.append(content.trim());
    }

    private String getOptionsText(Set<Long> optionIds) {
        StringBuilder result = new StringBuilder();

        for (AnswerOption option : answerOptions) {
            if (optionIds.contains(option.getId())) {
                if (result.length() > 0) {
                    result.append("\n");
                }

                result.append(getOptionDisplayText(option));
            }
        }

        return result.toString();
    }

    private void showFeedback(
            String title,
            String details,
            boolean isCorrect
    ) {
        tvFeedbackTitle.setText(title);

        tvFeedbackDetails.setText(details);

        if (isCorrect) {
            tvFeedbackTitle.setTextColor(
                    getColor(R.color.kb_primary)
            );
        } else {
            tvFeedbackTitle.setTextColor(
                    getColor(R.color.kb_text_primary)
            );
        }

        Button study = feedbackContainer.findViewWithTag("explanation-button");
        if (study == null) { study = new Button(this); study.setTag("explanation-button"); study.setText("Բացատրություն և աղբյուր"); feedbackContainer.addView(study); }
        final long questionForStudy = currentQuestion.getId();
        study.setOnClickListener(v -> StudyContent.showExplanation(this, questionForStudy));
        feedbackContainer.setVisibility(View.VISIBLE);
    }

    private void disableAnswerOptions() {
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);

            if (!"study-tool".equals(child.getTag())) child.setEnabled(false);

            if (child instanceof RadioGroup) {
                RadioGroup group = (RadioGroup) child;

                for (int j = 0; j < group.getChildCount(); j++) {
                    group.getChildAt(j).setEnabled(false);
                }
            }

            if (child instanceof CheckBox) {
                if (!"study-tool".equals(child.getTag())) child.setEnabled(false);
            }
        }
    }

    // ---------------------------------------------------------
    // Сохранение ответа
    // ---------------------------------------------------------

    private void saveCurrentAnswer(Boolean isCorrect) {
        String selectedAnswer = serializeSelectedAnswer();

        if (selectedAnswer.isEmpty()) {
            return;
        }

        SessionAnswer existingAnswer =
                sessionAnswerRepository.getAnswerByAttempt(
                        sessionId,
                        currentQuestion.getId(),
                        1
                );

        if (existingAnswer == null) {
            SessionAnswer newAnswer = new SessionAnswer(
                    sessionId,
                    currentQuestion.getId(),
                    selectedAnswer,
                    isCorrect,
                    1
            );

            sessionAnswerRepository.insertAnswer(newAnswer);

        } else {
            existingAnswer.setSelectedAnswer(selectedAnswer);

            existingAnswer.setIsCorrect(isCorrect);

            existingAnswer.setAnsweredAt(getCurrentDateTime());

            sessionAnswerRepository.updateAnswer(existingAnswer);
        }
    }

    private String serializeSelectedAnswer() {
        if (isTyped(currentQuestion)) return typedInput == null ? "" : typedInput.getText().toString().trim();
        if (selectedOptionIds.isEmpty()) {
            return "";
        }

        TreeSet<Long> sortedIds = new TreeSet<>(selectedOptionIds);

        StringBuilder result = new StringBuilder();

        for (Long id : sortedIds) {
            if (result.length() > 0) {
                result.append(",");
            }

            result.append(id);
        }

        return result.toString();
    }

    private Set<Long> parseAnswerIds(String value) {
        Set<Long> result = new HashSet<>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] ids = value.split(",");

        for (String id : ids) {
            try {
                long parsedId = Long.parseLong(id.trim());

                if (parsedId > 0) {
                    result.add(parsedId);
                }

            } catch (NumberFormatException ignored) {
                // Некорректное значение не учитывается.
            }
        }

        return result;
    }

    private String getCurrentDateTime() {
        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        java.util.Locale.US
                );

        return formatter.format(new java.util.Date());
    }

    // ---------------------------------------------------------
    // Навигация между вопросами
    // ---------------------------------------------------------

    private void showPreviousQuestion() {
        if (currentQuestionIndex <= 0) {
            return;
        }

        saveBeforeNavigation();

        showQuestion(currentQuestionIndex - 1);
    }

    private void showNextQuestion() {
        if (currentQuestionIndex >= questionIds.size() - 1) {
            return;
        }

        saveBeforeNavigation();

        showQuestion(currentQuestionIndex + 1);
    }

    private void saveBeforeNavigation() {
        if (currentQuestion == null || serializeSelectedAnswer().isEmpty()) {
            return;
        }

        String feedbackMode = currentSession.getFeedbackMode();

        if (TrainingSession.FEEDBACK_IMMEDIATE.equals(feedbackMode)) {
            SessionAnswer existing =
                    sessionAnswerRepository.getAnswerByAttempt(
                            sessionId,
                            currentQuestion.getId(),
                            1
                    );

            Boolean correctness = existing == null
                    ? null
                    : existing.getIsCorrect();

            saveCurrentAnswer(correctness);

        } else {
            saveCurrentAnswer(null);
        }
    }

    private void updateNavigationButtons() {
        btnPrevious.setEnabled(currentQuestionIndex > 0);

        boolean isLastQuestion =
                currentQuestionIndex >= questionIds.size() - 1;

        btnNext.setEnabled(!isLastQuestion);

        btnNext.setVisibility(
                isLastQuestion ? View.GONE : View.VISIBLE
        );

        btnFinish.setVisibility(View.VISIBLE);
    }

    // ---------------------------------------------------------
    // Завершение тренировки
    // ---------------------------------------------------------

    private void confirmFinish() {
        new AlertDialog.Builder(this)
                .setTitle(
                        localized(
                                "Завершить тренировку?",
                                "Finish training?",
                                "Ավարտե՞լ վարժանքը։"
                        )
                )
                .setMessage(
                        localized(
                                "После завершения результаты будут сохранены.",
                                "Your results will be saved after finishing.",
                                "Ավարտելուց հետո արդյունքները կպահպանվեն։"
                        )
                )
                .setNegativeButton(
                        localized("Продолжить", "Continue", "Շարունակել"),
                        null
                )
                .setPositiveButton(
                        localized("Завершить", "Finish", "Ավարտել"),
                        (dialog, which) -> finishTraining()
                )
                .show();
    }

    private void finishTraining() {
        saveBeforeNavigation();

        int correctCount = 0;
        int incorrectCount = 0;

        String feedbackMode = currentSession.getFeedbackMode();

        if (TrainingSession.FEEDBACK_DEFERRED.equals(feedbackMode)) {
            int[] results = calculateDeferredResults();

            correctCount = results[0];
            incorrectCount = results[1];

        } else {
            int[] results = calculateImmediateResults();

            correctCount = results[0];
            incorrectCount = results[1];
        }

        trainingSessionRepository.updateResults(
                sessionId,
                correctCount,
                incorrectCount
        );

        trainingSessionRepository.finishSession(sessionId);
        currentSession.setStatus(TrainingSession.STATUS_COMPLETED);

        int ungradedCount = questionIds.size()
                - correctCount
                - incorrectCount;

        String resultMessage = localized(
                "Тренировка завершена.\nПравильно: "
                        + correctCount
                        + "\nНеправильно: "
                        + incorrectCount
                        + "\nБез оценки: "
                        + ungradedCount,

                "Training completed.\nCorrect: "
                        + correctCount
                        + "\nIncorrect: "
                        + incorrectCount
                        + "\nUngraded: "
                        + ungradedCount,

                "Վարժանքն ավարտված է։\nՃիշտ՝ "
                        + correctCount
                        + "\nՍխալ՝ "
                        + incorrectCount
                        + "\nԱռանց գնահատականի՝ "
                        + ungradedCount
        );

        Toast.makeText(
                this,
                resultMessage,
                Toast.LENGTH_LONG
        ).show();

        android.content.Intent resultsIntent = new android.content.Intent(this, LearningHubActivity.class);
        resultsIntent.putExtra("mode", "history");
        startActivity(resultsIntent);
        finish();
    }

    private int[] calculateImmediateResults() {
        int correct = 0;
        int incorrect = 0;

        for (Long questionId : questionIds) {
            SessionAnswer answer =
                    sessionAnswerRepository.getAnswerByAttempt(
                            sessionId,
                            questionId,
                            1
                    );

            if (answer == null || answer.getIsCorrect() == null) {
                continue;
            }

            if (answer.getIsCorrect()) {
                correct++;
            } else {
                incorrect++;
            }
        }

        return new int[]{correct, incorrect};
    }

    private int[] calculateDeferredResults() {
        int correct = 0;
        int incorrect = 0;

        for (Long questionId : questionIds) {
            SessionAnswer savedAnswer =
                    sessionAnswerRepository.getAnswerByAttempt(
                            sessionId,
                            questionId,
                            1
                    );

            if (savedAnswer == null) {
                continue;
            }

            Question typedQuestion = questionRepository.getQuestionById(questionId);
            if (isTyped(typedQuestion)) {
                Boolean grade = gradeTyped(typedQuestion, savedAnswer.getSelectedAnswer());
                savedAnswer.setIsCorrect(grade);
                sessionAnswerRepository.updateAnswer(savedAnswer);
                if (Boolean.TRUE.equals(grade)) correct++;
                else if (Boolean.FALSE.equals(grade)) incorrect++;
                continue;
            }
            Set<Long> selectedIds = parseAnswerIds(
                    savedAnswer.getSelectedAnswer()
            );

            if (selectedIds.isEmpty()) {
                continue;
            }

            Question question =
                    questionRepository.getQuestionById(questionId);

            if (question == null) {
                continue;
            }

            List<CorrectAnswer> correctAnswers =
                    correctAnswerRepository.getCorrectAnswersByQuestionId(
                            questionId
                    );

            String questionStatus = question.getSourceKeyStatus();

            if (!Question.KEY_VERIFIED.equals(questionStatus)
                    || correctAnswers == null
                    || correctAnswers.isEmpty()) {
                continue;
            }

            Set<Long> correctIds = new HashSet<>();

            boolean verified = true;

            for (CorrectAnswer correctAnswer : correctAnswers) {
                if (!CorrectAnswer.STATUS_VERIFIED.equals(
                        correctAnswer.getVerificationStatus()
                )) {
                    verified = false;
                    break;
                }

                if (!CorrectAnswer.TYPE_OPTION.equals(
                        correctAnswer.getAnswerType()
                ) || correctAnswer.getOptionId() == null) {
                    verified = false;
                    break;
                }

                correctIds.add(correctAnswer.getOptionId());
            }

            if (!verified || correctIds.isEmpty()) {
                continue;
            }

            boolean isCorrect = selectedIds.equals(correctIds);

            savedAnswer.setIsCorrect(isCorrect);

            savedAnswer.setAnsweredAt(getCurrentDateTime());

            sessionAnswerRepository.updateAnswer(savedAnswer);

            if (isCorrect) {
                correct++;
            } else {
                incorrect++;
            }
        }

        return new int[]{correct, incorrect};
    }

    // ---------------------------------------------------------
    // Выход из тренировки
    // ---------------------------------------------------------

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle(
                        localized(
                                "Выйти из тренировки?",
                                "Leave training?",
                                "Դո՞ւրս գալ վարժանքից։"
                        )
                )
                .setMessage(
                        localized(
                                "Сохранённые ответы останутся в базе. Тренировка останется незавершённой.",
                                "Saved answers will remain in the database. The session will remain unfinished.",
                                "Պահպանված պատասխանները կմնան տվյալների բազայում։ Վարժանքը կմնա անավարտ։"
                        )
                )
                .setNegativeButton(
                        localized("Остаться", "Stay", "Մնալ"),
                        null
                )
                .setPositiveButton(
                        localized("Выйти", "Leave", "Դուրս գալ"),
                        (dialog, which) -> {
                            saveBeforeNavigation();
                            finish();
                        }
                )
                .show();
    }

    private void showMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);

        popupMenu.getMenu().add(
                localized(
                        "Завершить тренировку",
                        "Finish training",
                        "Ավարտել վարժանքը"
                )
        );

        popupMenu.setOnMenuItemClickListener(item -> {
            confirmFinish();
            return true;
        });

        popupMenu.show();
    }

    // ---------------------------------------------------------
    // Вспомогательные методы
    // ---------------------------------------------------------

    private String getOptionDisplayText(AnswerOption option) {
        String label = option.getOptionLabel();

        if (label == null || label.trim().isEmpty()) {
            label = String.valueOf(option.getSortOrder() + 1);
        }

        return label + ". " + safeText(option.getOptionText());
    }

    private void showErrorAndClose(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();

        finish();
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
}
