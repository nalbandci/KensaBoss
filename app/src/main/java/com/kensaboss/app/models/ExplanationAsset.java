
package com.kensaboss.app.models;

public class ExplanationAsset {

    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_DIAGRAM = "DIAGRAM";
    public static final String TYPE_TABLE = "TABLE";
    public static final String TYPE_FORMULA = "FORMULA";
    public static final String TYPE_CHART = "CHART";
    public static final String TYPE_OTHER = "OTHER";

    private long id;
    private long explanationId;
    private String assetType;
    private String filePath;
    private String description;
    private int sortOrder;

    public ExplanationAsset(
            long explanationId,
            String assetType,
            String filePath,
            String description,
            int sortOrder
    ) {
        this.explanationId = explanationId;
        this.assetType = assetType;
        this.filePath = filePath;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExplanationId() {
        return explanationId;
    }

    public void setExplanationId(long explanationId) {
        this.explanationId = explanationId;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
