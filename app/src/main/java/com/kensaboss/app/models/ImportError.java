
package com.kensaboss.app.models;

public class ImportError {

    // Типы объектов, при импорте которых может возникнуть ошибка
    public static final String ENTITY_PACK = "PACK";
    public static final String ENTITY_BOOK = "BOOK";
    public static final String ENTITY_BOOK_PART = "BOOK_PART";
    public static final String ENTITY_CHAPTER = "CHAPTER";
    public static final String ENTITY_SECTION = "SECTION";
    public static final String ENTITY_QUESTION = "QUESTION";
    public static final String ENTITY_ANSWER_OPTION = "ANSWER_OPTION";
    public static final String ENTITY_CORRECT_ANSWER = "CORRECT_ANSWER";
    public static final String ENTITY_EXPLANATION = "EXPLANATION";
    public static final String ENTITY_ASSET = "ASSET";

    // Уровни серьёзности
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_ERROR = "ERROR";
    public static final String SEVERITY_CRITICAL = "CRITICAL";

    private long id;

    private long importId;

    private String entityType;
    private String externalId;

    private Integer sourcePage;

    private String severity;
    private String message;

    private String createdAt;

    public ImportError(
            long importId,
            String entityType,
            String externalId,
            Integer sourcePage,
            String severity,
            String message
    ) {
        this.importId = importId;
        this.entityType = entityType;
        this.externalId = externalId;
        this.sourcePage = sourcePage;
        this.severity = severity;
        this.message = message;

        this.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(new java.util.Date());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getImportId() {
        return importId;
    }

    public void setImportId(long importId) {
        this.importId = importId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Integer getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(Integer sourcePage) {
        this.sourcePage = sourcePage;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
