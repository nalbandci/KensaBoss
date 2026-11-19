
package com.kensaboss.app.models;

public class ImportHistory {

    // Статусы импорта
    public static final String STATUS_STARTED = "STARTED";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_PARTIAL = "PARTIAL";
    public static final String STATUS_FAILED = "FAILED";

    private long id;

    private String packUid;
    private int packVersion;

    private String fileName;
    private String checksum;

    private String status;

    private int importedCount;
    private int errorCount;

    private String startedAt;
    private String finishedAt;

    public ImportHistory(
            String packUid,
            int packVersion,
            String fileName,
            String checksum
    ) {
        this.packUid = packUid;
        this.packVersion = packVersion;
        this.fileName = fileName;
        this.checksum = checksum;

        this.status = STATUS_STARTED;

        this.importedCount = 0;
        this.errorCount = 0;

        this.startedAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(new java.util.Date());
        this.finishedAt = null;
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

    public int getPackVersion() {
        return packVersion;
    }

    public void setPackVersion(int packVersion) {
        this.packVersion = packVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }
}
