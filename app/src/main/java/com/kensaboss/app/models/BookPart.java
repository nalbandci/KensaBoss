
package com.kensaboss.app.models;

public class BookPart {

    private long id;
    private long bookId;

    private String externalId;
    private String title;

    private int partNumber;
    private int sortOrder;

    public BookPart(
            long bookId,
            String externalId,
            String title,
            int partNumber,
            int sortOrder
    ) {
        this.bookId = bookId;
        this.externalId = externalId;
        this.title = title;
        this.partNumber = partNumber;
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
    // BOOK ID
    // ==========================================

    public long getBookId() {
        return bookId;
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
    // PART NUMBER
    // ==========================================

    public int getPartNumber() {
        return partNumber;
    }

    // ==========================================
    // SORT ORDER
    // ==========================================

    public int getSortOrder() {
        return sortOrder;
    }
}
