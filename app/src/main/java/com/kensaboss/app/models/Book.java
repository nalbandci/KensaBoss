
package com.kensaboss.app.models;

public class Book {

    private long id;
    private Long packId;

    private String externalId;
    private String title;
    private String author;
    private String language;
    private String description;
    private String version;
    private String createdAt;

    public Book(
            Long packId,
            String externalId,
            String title,
            String author,
            String language,
            String description,
            String version
    ) {
        this.packId = packId;
        this.externalId = externalId;
        this.title = title;
        this.author = author;
        this.language = language;
        this.description = description;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getPackId() {
        return packId;
    }

    public String getExternalId() {
        return externalId;
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

    public String getVersion() {
        return version;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
