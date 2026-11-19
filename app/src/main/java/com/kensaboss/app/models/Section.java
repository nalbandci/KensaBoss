
package com.kensaboss.app.models;

public class Section {

    private long id;
    private long chapterId;

    private String externalId;
    private String title;

    private int sortOrder;

    public Section(
            long chapterId,
            String externalId,
            String title,
            int sortOrder
    ) {
        this.chapterId = chapterId;
        this.externalId = externalId;
        this.title = title;
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
    // CHAPTER ID
    // ==========================================

    public long getChapterId() {
        return chapterId;
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
    // SORT ORDER
    // ==========================================

    public int getSortOrder() {
        return sortOrder;
    }
}
