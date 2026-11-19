
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.ImportError;
import com.kensaboss.app.models.ImportHistory;

import java.util.ArrayList;
import java.util.List;

public class ImportRepository {

    private final DatabaseHelper databaseHelper;

    public ImportRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // =====================================================
    // IMPORT HISTORY
    // =====================================================

    public long insertImportHistory(ImportHistory history) {

        validateHistory(history);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ImportHistory.PACK_UID,
                history.getPackUid()
        );

        values.put(
                DatabaseContract.ImportHistory.PACK_VERSION,
                history.getPackVersion()
        );

        values.put(
                DatabaseContract.ImportHistory.FILE_NAME,
                history.getFileName()
        );

        values.put(
                DatabaseContract.ImportHistory.CHECKSUM,
                history.getChecksum()
        );

        values.put(
                DatabaseContract.ImportHistory.STATUS,
                history.getStatus()
        );

        values.put(
                DatabaseContract.ImportHistory.IMPORTED_COUNT,
                history.getImportedCount()
        );

        values.put(
                DatabaseContract.ImportHistory.ERROR_COUNT,
                history.getErrorCount()
        );

        values.put(
                DatabaseContract.ImportHistory.STARTED_AT,
                history.getStartedAt()
        );

        values.put(
                DatabaseContract.ImportHistory.FINISHED_AT,
                history.getFinishedAt()
        );

        long id = db.insertOrThrow(
                DatabaseContract.ImportHistory.TABLE,
                null,
                values
        );

        history.setId(id);

        return id;
    }

    public ImportHistory getImportHistoryById(long importId) {

        if (importId <= 0) {
            return null;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ImportHistory.TABLE,
                null,
                DatabaseContract.ImportHistory.ID + " = ?",
                new String[]{String.valueOf(importId)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readImportHistory(cursor);
            }
        }

        return null;
    }

    public List<ImportHistory> getAllImportHistory() {

        List<ImportHistory> historyList = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ImportHistory.TABLE,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.ImportHistory.ID + " DESC"
        )) {

            while (cursor.moveToNext()) {
                historyList.add(readImportHistory(cursor));
            }
        }

        return historyList;
    }

    public List<ImportHistory> getImportHistoryByPackUid(String packUid) {

        if (packUid == null || packUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Pack UID cannot be empty");
        }

        List<ImportHistory> historyList = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ImportHistory.TABLE,
                null,
                DatabaseContract.ImportHistory.PACK_UID + " = ?",
                new String[]{packUid},
                null,
                null,
                DatabaseContract.ImportHistory.ID + " DESC"
        )) {

            while (cursor.moveToNext()) {
                historyList.add(readImportHistory(cursor));
            }
        }

        return historyList;
    }

    public int updateImportHistory(ImportHistory history) {

        validateHistory(history);

        if (history.getId() <= 0) {
            throw new IllegalArgumentException("Import ID must be positive");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ImportHistory.PACK_UID,
                history.getPackUid()
        );

        values.put(
                DatabaseContract.ImportHistory.PACK_VERSION,
                history.getPackVersion()
        );

        values.put(
                DatabaseContract.ImportHistory.FILE_NAME,
                history.getFileName()
        );

        values.put(
                DatabaseContract.ImportHistory.CHECKSUM,
                history.getChecksum()
        );

        values.put(
                DatabaseContract.ImportHistory.STATUS,
                history.getStatus()
        );

        values.put(
                DatabaseContract.ImportHistory.IMPORTED_COUNT,
                history.getImportedCount()
        );

        values.put(
                DatabaseContract.ImportHistory.ERROR_COUNT,
                history.getErrorCount()
        );

        values.put(
                DatabaseContract.ImportHistory.STARTED_AT,
                history.getStartedAt()
        );

        values.put(
                DatabaseContract.ImportHistory.FINISHED_AT,
                history.getFinishedAt()
        );

        return db.update(
                DatabaseContract.ImportHistory.TABLE,
                values,
                DatabaseContract.ImportHistory.ID + " = ?",
                new String[]{String.valueOf(history.getId())}
        );
    }

    public int finishImport(
            long importId,
            String status,
            int importedCount,
            int errorCount
    ) {

        if (importId <= 0) {
            throw new IllegalArgumentException("Import ID must be positive");
        }

        if (importedCount < 0 || errorCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }

        if (!ImportHistory.STATUS_SUCCESS.equals(status)
                && !ImportHistory.STATUS_PARTIAL.equals(status)
                && !ImportHistory.STATUS_FAILED.equals(status)) {

            throw new IllegalArgumentException("Invalid final import status");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.ImportHistory.STATUS, status);

        values.put(
                DatabaseContract.ImportHistory.IMPORTED_COUNT,
                importedCount
        );

        values.put(
                DatabaseContract.ImportHistory.ERROR_COUNT,
                errorCount
        );

        values.put(
                DatabaseContract.ImportHistory.FINISHED_AT,
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(new java.util.Date())
        );

        return db.update(
                DatabaseContract.ImportHistory.TABLE,
                values,
                DatabaseContract.ImportHistory.ID + " = ?",
                new String[]{String.valueOf(importId)}
        );
    }

    public int deleteImportHistoryById(long importId) {

        if (importId <= 0) {
            return 0;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.ImportHistory.TABLE,
                DatabaseContract.ImportHistory.ID + " = ?",
                new String[]{String.valueOf(importId)}
        );
    }

    public int countImportHistory() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM "
                        + DatabaseContract.ImportHistory.TABLE,
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // IMPORT ERRORS
    // =====================================================

    public long insertImportError(ImportError error) {

        validateError(error);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ImportErrors.IMPORT_ID,
                error.getImportId()
        );

        values.put(
                DatabaseContract.ImportErrors.ENTITY_TYPE,
                error.getEntityType()
        );

        values.put(
                DatabaseContract.ImportErrors.EXTERNAL_ID,
                error.getExternalId()
        );

        if (error.getSourcePage() == null) {
            values.putNull(DatabaseContract.ImportErrors.SOURCE_PAGE);
        } else {
            values.put(
                    DatabaseContract.ImportErrors.SOURCE_PAGE,
                    error.getSourcePage()
            );
        }

        values.put(
                DatabaseContract.ImportErrors.SEVERITY,
                error.getSeverity()
        );

        values.put(
                DatabaseContract.ImportErrors.MESSAGE,
                error.getMessage()
        );

        values.put(
                DatabaseContract.ImportErrors.CREATED_AT,
                error.getCreatedAt()
        );

        long id = db.insertOrThrow(
                DatabaseContract.ImportErrors.TABLE,
                null,
                values
        );

        error.setId(id);

        return id;
    }

    public ImportError getImportErrorById(long errorId) {

        if (errorId <= 0) {
            return null;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ImportErrors.TABLE,
                null,
                DatabaseContract.ImportErrors.ID + " = ?",
                new String[]{String.valueOf(errorId)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readImportError(cursor);
            }
        }

        return null;
    }

    public List<ImportError> getErrorsByImportId(long importId) {

        if (importId <= 0) {
            throw new IllegalArgumentException("Import ID must be positive");
        }

        List<ImportError> errors = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ImportErrors.TABLE,
                null,
                DatabaseContract.ImportErrors.IMPORT_ID + " = ?",
                new String[]{String.valueOf(importId)},
                null,
                null,
                DatabaseContract.ImportErrors.ID + " ASC"
        )) {

            while (cursor.moveToNext()) {
                errors.add(readImportError(cursor));
            }
        }

        return errors;
    }

    public List<ImportError> getErrorsBySeverity(String severity) {

        if (severity == null || severity.trim().isEmpty()) {
            throw new IllegalArgumentException("Severity cannot be empty");
        }

        List<ImportError> errors = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ImportErrors.TABLE,
                null,
                DatabaseContract.ImportErrors.SEVERITY + " = ?",
                new String[]{severity},
                null,
                null,
                DatabaseContract.ImportErrors.ID + " DESC"
        )) {

            while (cursor.moveToNext()) {
                errors.add(readImportError(cursor));
            }
        }

        return errors;
    }

    public int deleteImportErrorById(long errorId) {

        if (errorId <= 0) {
            return 0;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.ImportErrors.TABLE,
                DatabaseContract.ImportErrors.ID + " = ?",
                new String[]{String.valueOf(errorId)}
        );
    }

    public int deleteErrorsByImportId(long importId) {

        if (importId <= 0) {
            return 0;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.ImportErrors.TABLE,
                DatabaseContract.ImportErrors.IMPORT_ID + " = ?",
                new String[]{String.valueOf(importId)}
        );
    }

    public int countImportErrors() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM "
                        + DatabaseContract.ImportErrors.TABLE,
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // CURSOR MAPPING
    // =====================================================

    private ImportHistory readImportHistory(Cursor cursor) {

        ImportHistory history = new ImportHistory(
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportHistory.PACK_UID
                )),
                cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportHistory.PACK_VERSION
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportHistory.FILE_NAME
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportHistory.CHECKSUM
                ))
        );

        history.setId(cursor.getLong(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportHistory.ID
        )));

        history.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportHistory.STATUS
        )));

        history.setImportedCount(cursor.getInt(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportHistory.IMPORTED_COUNT
        )));

        history.setErrorCount(cursor.getInt(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportHistory.ERROR_COUNT
        )));

        history.setStartedAt(cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportHistory.STARTED_AT
        )));

        history.setFinishedAt(cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportHistory.FINISHED_AT
        )));

        return history;
    }

    private ImportError readImportError(Cursor cursor) {

        int pageIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportErrors.SOURCE_PAGE
        );

        Integer sourcePage = cursor.isNull(pageIndex)
                ? null
                : cursor.getInt(pageIndex);

        ImportError error = new ImportError(
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportErrors.IMPORT_ID
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportErrors.ENTITY_TYPE
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportErrors.EXTERNAL_ID
                )),
                sourcePage,
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportErrors.SEVERITY
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ImportErrors.MESSAGE
                ))
        );

        error.setId(cursor.getLong(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportErrors.ID
        )));

        error.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseContract.ImportErrors.CREATED_AT
        )));

        return error;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateHistory(ImportHistory history) {

        if (history == null) {
            throw new IllegalArgumentException("Import history cannot be null");
        }

        if (history.getPackUid() == null
                || history.getPackUid().trim().isEmpty()) {
            throw new IllegalArgumentException("Pack UID cannot be empty");
        }

        if (history.getPackVersion() <= 0) {
            throw new IllegalArgumentException("Pack version must be positive");
        }

        if (history.getStatus() == null
                || history.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Import status cannot be empty");
        }

        if (history.getImportedCount() < 0 || history.getErrorCount() < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }

        if (history.getStartedAt() == null
                || history.getStartedAt().trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be empty");
        }
    }

    private void validateError(ImportError error) {

        if (error == null) {
            throw new IllegalArgumentException("Import error cannot be null");
        }

        if (error.getImportId() <= 0) {
            throw new IllegalArgumentException("Import ID must be positive");
        }

        if (error.getEntityType() == null
                || error.getEntityType().trim().isEmpty()) {
            throw new IllegalArgumentException("Entity type cannot be empty");
        }

        if (error.getSeverity() == null
                || error.getSeverity().trim().isEmpty()) {
            throw new IllegalArgumentException("Severity cannot be empty");
        }

        if (error.getMessage() == null
                || error.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be empty");
        }

        if (error.getCreatedAt() == null
                || error.getCreatedAt().trim().isEmpty()) {
            throw new IllegalArgumentException("Creation date cannot be empty");
        }

        if (error.getSourcePage() != null && error.getSourcePage() < 1) {
            throw new IllegalArgumentException("Source page must be positive");
        }
    }

    // =====================================================
    // CLOSE
    // =====================================================

    public void close() {
        databaseHelper.close();
    }
}
