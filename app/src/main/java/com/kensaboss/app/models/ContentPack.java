
package com.kensaboss.app.models;



public class ContentPack {

    private long id;

    private String packUid;
    private String title;
    private String language;
    private int version;
    private String description;
    private String checksum;
    private String createdAt;

    public ContentPack(
            String packUid,
            String title,
            String language,
            int version,
            String description,
            String checksum
    ) {

        this.packUid = packUid;
        this.title = title;
        this.language = language;
        this.version = version;
        this.description = description;
        this.checksum = checksum;

        this.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(new java.util.Date());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackUid() {
        return packUid;
    }

    public void setPackUid(String packUid) {
        this.packUid = packUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
