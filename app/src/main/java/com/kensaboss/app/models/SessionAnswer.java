
package com.kensaboss.app.models;

public class SessionAnswer {

    private long id;
    private long sessionId;
    private long questionId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private String answeredAt;
    private int attemptNumber;

    public SessionAnswer(
            long sessionId,
            long questionId,
            String selectedAnswer,
            Boolean isCorrect,
            int attemptNumber
    ) {
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
        this.attemptNumber = attemptNumber;
        this.answeredAt = getCurrentDateTime();
    }

    private String getCurrentDateTime() {

        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        java.util.Locale.US
                );

        return formatter.format(new java.util.Date());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(String answeredAt) {
        this.answeredAt = answeredAt;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
}
