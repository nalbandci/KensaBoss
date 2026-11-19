
package com.kensaboss.app.models;

public class Explanation {

    private long id;
    private long questionId;

    private String language;
    private String introduction;
    private String detailedText;
    private String solutionSteps;
    private String additionalNotes;

    private int version;

    public Explanation(
            long questionId,
            String language,
            String introduction,
            String detailedText,
            String solutionSteps,
            String additionalNotes,
            int version
    ) {
        this.questionId = questionId;
        this.language = language;
        this.introduction = introduction;
        this.detailedText = detailedText;
        this.solutionSteps = solutionSteps;
        this.additionalNotes = additionalNotes;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getDetailedText() {
        return detailedText;
    }

    public void setDetailedText(String detailedText) {
        this.detailedText = detailedText;
    }

    public String getSolutionSteps() {
        return solutionSteps;
    }

    public void setSolutionSteps(String solutionSteps) {
        this.solutionSteps = solutionSteps;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
