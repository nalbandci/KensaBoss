
package com.kensaboss.app.models;

public class Question {

    // ==========================================
    // 1. ТИПЫ ВОПРОСОВ
    // ==========================================

    public static final String TYPE_SINGLE_CHOICE = "SINGLE_CHOICE";
    public static final String TYPE_MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String TYPE_MATCHING = "MATCHING";
    public static final String TYPE_TRUE_FALSE = "TRUE_FALSE";
    public static final String TYPE_SEQUENCE = "SEQUENCE";
    public static final String TYPE_NUMERIC = "NUMERIC";
    public static final String TYPE_MULTI_PART = "MULTI_PART";
    public static final String TYPE_TEXT = "TEXT";

    // ==========================================
    // 2. СТАТУСЫ ПРОВЕРКИ ПРАВИЛЬНОГО ОТВЕТА
    // ==========================================

    public static final String KEY_UNVERIFIED = "UNVERIFIED";
    public static final String KEY_VERIFIED = "VERIFIED";
    public static final String KEY_AMBIGUOUS = "AMBIGUOUS";
    public static final String KEY_MISSING = "MISSING";
    public static final String KEY_CONFLICTING = "CONFLICTING";
    public static final String KEY_PRINTING_ERROR = "PRINTING_ERROR";

    // ==========================================
    // 3. ПОЛЯ ВОПРОСА
    // ==========================================

    private long id;

    private long bookId;
    private long chapterId;
    private Long sectionId;

    private String externalId;

    private String questionText;
    private String questionType;

    private String originalNumber;
    private Integer sourcePage;

    private String difficulty;
    private int sortOrder;

    private String sourceKeyStatus;

    // ==========================================
    // 4. КОНСТРУКТОР
    // ==========================================

    public Question(
            long bookId,
            long chapterId,
            Long sectionId,
            String externalId,
            String questionText,
            String questionType,
            String originalNumber,
            Integer sourcePage,
            String difficulty,
            int sortOrder,
            String sourceKeyStatus
    ) {

        this.bookId = bookId;
        this.chapterId = chapterId;
        this.sectionId = sectionId;

        this.externalId = externalId;

        this.questionText = questionText;
        this.questionType = questionType;

        this.originalNumber = originalNumber;
        this.sourcePage = sourcePage;

        this.difficulty = difficulty;
        this.sortOrder = sortOrder;

        this.sourceKeyStatus = sourceKeyStatus;
    }

    // ==========================================
    // 5. GETTERS — ПОЛУЧЕНИЕ ДАННЫХ
    // ==========================================

    public long getId() {
        return id;
    }

    public long getBookId() {
        return bookId;
    }

    public long getChapterId() {
        return chapterId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getOriginalNumber() {
        return originalNumber;
    }

    public Integer getSourcePage() {
        return sourcePage;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getSourceKeyStatus() {
        return sourceKeyStatus;
    }

    // ==========================================
    // 6. SETTERS — ИЗМЕНЕНИЕ ДАННЫХ
    // ==========================================

    public void setId(long id) {
        this.id = id;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public void setChapterId(long chapterId) {
        this.chapterId = chapterId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public void setOriginalNumber(String originalNumber) {
        this.originalNumber = originalNumber;
    }

    public void setSourcePage(Integer sourcePage) {
        this.sourcePage = sourcePage;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSourceKeyStatus(String sourceKeyStatus) {
        this.sourceKeyStatus = sourceKeyStatus;
    }
}
