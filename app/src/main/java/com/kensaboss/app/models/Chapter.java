
package com.kensaboss.app.models;

public class Chapter {

    private long id;
    private long bookPartId;

    private String externalId;
    private String title;

    private int chapterNumber;
    private int sortOrder;

    public Chapter(
            long bookPartId,
            String externalId,
            String title,
            int chapterNumber,
            int sortOrder
    ) {
        this.bookPartId = bookPartId;
        this.externalId = externalId;
        this.title = title;
        this.chapterNumber = chapterNumber;
        this.sortOrder = sortOrder;
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
    // BOOK PART ID
    // ==========================================

    public long getBookPartId() {
        return bookPartId;
    }

    // ==========================================
    // EXTERNAL ID
    // ==========================================

    public String getExternalId() {
        return externalId;
    }

    // ==========================================
    // TITLE
    // ==========================================

    public String getTitle() {
        return title;
    }

    // ==========================================
    // CHAPTER NUMBER
    // ==========================================

    public int getChapterNumber() {
        return chapterNumber;
    }

    // ==========================================
    // SORT ORDER
    // ==========================================

    public int getSortOrder() {
        return sortOrder;
    }
}
