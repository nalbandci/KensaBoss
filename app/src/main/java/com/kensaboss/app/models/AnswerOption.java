
package com.kensaboss.app.models;

public class AnswerOption {

    // ==========================================
    // ПОЛЯ МОДЕЛИ
    // ==========================================

    private long id;

    private long questionId;

    private String externalId;

    private String optionText;

    private String optionLabel;

    private int sortOrder;


    // ==========================================
    // КОНСТРУКТОР
    // ==========================================

    public AnswerOption(
            long questionId,
            String externalId,
            String optionText,
            String optionLabel,
            int sortOrder
    ) {

        this.questionId = questionId;
        this.externalId = externalId;
        this.optionText = optionText;
        this.optionLabel = optionLabel;
        this.sortOrder = sortOrder;
    }


    // ==========================================
    // GETTERS
    // ==========================================

    public long getId() {
        return id;
    }

    public long getQuestionId() {
        return questionId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getOptionText() {
        return optionText;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public int getSortOrder() {
        return sortOrder;
    }


    // ==========================================
    // SETTERS
    // ==========================================

    public void setId(long id) {
        this.id = id;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
