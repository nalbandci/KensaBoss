
package com.kensaboss.app.models;

public class TrainingSession {

    // =====================================================
    // FEEDBACK MODES
    // =====================================================

    // Показывать результат сразу после каждого ответа.
    public static final String FEEDBACK_IMMEDIATE = "IMMEDIATE";

    // Показывать результаты только после завершения сессии.
    public static final String FEEDBACK_DEFERRED = "DEFERRED";


    // =====================================================
    // QUESTION ORDER
    // =====================================================

    public static final String ORDER_RANDOM = "RANDOM";

    public static final String ORDER_SEQUENTIAL = "SEQUENTIAL";


    // =====================================================
    // SESSION STATUS
    // =====================================================

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    public static final String STATUS_COMPLETED = "COMPLETED";

    public static final String STATUS_CANCELLED = "CANCELLED";


    // =====================================================
    // FIELDS
    // =====================================================

    private long id;

    private String feedbackMode;

    private String questionOrder;

    private int totalQuestions;

    private int correctCount;

    private int incorrectCount;

    private String status;

    private String startedAt;

    private String finishedAt;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public TrainingSession(
            String feedbackMode,
            String questionOrder,
            int totalQuestions
    ) {

        this.feedbackMode = feedbackMode;
        this.questionOrder = questionOrder;
        this.totalQuestions = totalQuestions;

        this.correctCount = 0;
        this.incorrectCount = 0;

        this.status = STATUS_IN_PROGRESS;

        this.startedAt = getCurrentDateTime();

        this.finishedAt = null;
    }


    // =====================================================
    // DATE AND TIME
    // =====================================================

    private String getCurrentDateTime() {

        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        java.util.Locale.US
                );

        return formatter.format(new java.util.Date());
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public long getId() {
        return id;
    }

    public String getFeedbackMode() {
        return feedbackMode;
    }

    public String getQuestionOrder() {
        return questionOrder;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public String getStatus() {
        return status;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setId(long id) {
        this.id = id;
    }

    public void setFeedbackMode(String feedbackMode) {
        this.feedbackMode = feedbackMode;
    }

    public void setQuestionOrder(String questionOrder) {
        this.questionOrder = questionOrder;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }
}
