package com.kensaboss.app.models;

public class CorrectAnswer {

    // Типы ответов
    public static final String TYPE_OPTION = "OPTION";
    public static final String TYPE_NUMERIC = "NUMERIC";
    public static final String TYPE_TEXT = "TEXT";

    // Статусы проверки правильного ответа
    public static final String STATUS_NOT_VERIFIED = "NOT_VERIFIED";
    public static final String STATUS_VERIFIED = "VERIFIED";
    public static final String STATUS_AMBIGUOUS = "AMBIGUOUS";
    public static final String STATUS_CONFLICTING = "CONFLICTING";
    public static final String STATUS_PRINTING_ERROR = "PRINTING_ERROR";

    // Идентификаторы
    private long id;
    private long questionId;

    // Вариант ответа, если ответ связан с конкретной опцией
    private Long optionId;

    // Значение ответа, если это текстовый или числовой ответ
    private String answerValue;

    // Дополнительная информация
    private String answerType;
    private int sortOrder;

    // Данные из оригинального источника и проверки
    private String sourceValue;
    private String verificationStatus;
    private String verificationNotes;
    private String verifiedAt;

    public CorrectAnswer(
            long questionId,
            Long optionId,
            String answerValue,
            String answerType,
            int sortOrder
    ) {
        this.questionId = questionId;
        this.optionId = optionId;
        this.answerValue = answerValue;
        this.answerType = answerType;
        this.sortOrder = sortOrder;

        this.verificationStatus = STATUS_NOT_VERIFIED;
    }

    // ==========================================
    // ID
    // ==========================================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // ==========================================
    // QUESTION ID
    // ==========================================

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    // ==========================================
    // OPTION ID
    // ==========================================

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    // ==========================================
    // ANSWER VALUE
    // ==========================================

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }

    // ==========================================
    // ANSWER TYPE
    // ==========================================

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    // ==========================================
    // SORT ORDER
    // ==========================================

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ==========================================
    // SOURCE VALUE
    // ==========================================

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    // ==========================================
    // VERIFICATION STATUS
    // ==========================================

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    // ==========================================
    // VERIFICATION NOTES
    // ==========================================

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    // ==========================================
    // VERIFIED AT
    // ==========================================

    public String getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(String verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    public boolean isOptionAnswer() {
        return optionId != null;
    }

    public boolean isNumericAnswer() {
        return TYPE_NUMERIC.equals(answerType);
    }

    public boolean isTextAnswer() {
        return TYPE_TEXT.equals(answerType);
    }

    public boolean hasOptionId() {
        return optionId != null;
    }

    public boolean hasAnswerValue() {
        return answerValue != null && !answerValue.trim().isEmpty();
    }
}