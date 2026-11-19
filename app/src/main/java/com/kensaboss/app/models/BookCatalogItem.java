
package com.kensaboss.app.models;

public class BookCatalogItem {

    private final long id;
    private final String title;
    private final String author;
    private final String language;
    private final String description;

    private final int partsCount;
    private final int chaptersCount;
    private final int questionsCount;

    public BookCatalogItem(
            long id,
            String title,
            String author,
            String language,
            String description,
            int partsCount,
            int chaptersCount,
            int questionsCount
    ) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.language = language;
        this.description = description;
        this.partsCount = partsCount;
        this.chaptersCount = chaptersCount;
        this.questionsCount = questionsCount;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public int getChaptersCount() {
        return chaptersCount;
    }

    public int getQuestionsCount() {
        return questionsCount;
    }
}
