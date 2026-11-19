
package com.kensaboss.app.importer;

/**
 * Паспорт импортируемого пакета KensaBoss.
 *
 * Содержит основные сведения о файле .kbpack.
 */
public class PackManifest {

    private int schemaVersion;

    private String packUid;

    private String title;

    private String language;

    private int version;

    private String description;

    private String createdAt;

    public PackManifest(
            int schemaVersion,
            String packUid,
            String title,
            String language,
            int version,
            String description,
            String createdAt
    ) {
        this.schemaVersion = schemaVersion;
        this.packUid = packUid;
        this.title = title;
        this.language = language;
        this.version = version;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
