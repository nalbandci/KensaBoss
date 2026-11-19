
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.ContentPack;

import java.util.ArrayList;
import java.util.List;

public class ContentPackRepository {

    private final DatabaseHelper databaseHelper;

    public ContentPackRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // =====================================================
    // INSERT
    // =====================================================

    public long insertPack(ContentPack pack) {

        validatePack(pack);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createValues(pack);

        long id = db.insertOrThrow(
                DatabaseContract.ContentPacks.TABLE,
                null,
                values
        );

        pack.setId(id);

        return id;
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    public ContentPack getPackById(long packId) {

        if (packId <= 0) {
            return null;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ContentPacks.TABLE,
                null,
                DatabaseContract.ContentPacks.ID + " = ?",
                new String[]{String.valueOf(packId)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readPack(cursor);
            }
        }

        return null;
    }

    // =====================================================
    // GET BY UID
    // =====================================================

    public ContentPack getPackByUid(String packUid) {

        if (packUid == null || packUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Pack UID cannot be empty");
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ContentPacks.TABLE,
                null,
                DatabaseContract.ContentPacks.PACK_UID + " = ?",
                new String[]{packUid},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readPack(cursor);
            }
        }

        return null;
    }

    // =====================================================
    // GET ALL
    // =====================================================

    public List<ContentPack> getAllPacks() {

        List<ContentPack> packs = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ContentPacks.TABLE,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.ContentPacks.ID + " DESC"
        )) {

            while (cursor.moveToNext()) {
                packs.add(readPack(cursor));
            }
        }

        return packs;
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public int updatePack(ContentPack pack) {

        validatePack(pack);

        if (pack.getId() <= 0) {
            throw new IllegalArgumentException("Pack ID must be positive");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createValues(pack);

        return db.update(
                DatabaseContract.ContentPacks.TABLE,
                values,
                DatabaseContract.ContentPacks.ID + " = ?",
                new String[]{String.valueOf(pack.getId())}
        );
    }

    // =====================================================
    // DELETE
    // =====================================================

    public int deletePackById(long packId) {

        if (packId <= 0) {
            return 0;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.ContentPacks.TABLE,
                DatabaseContract.ContentPacks.ID + " = ?",
                new String[]{String.valueOf(packId)}
        );
    }

    // =====================================================
    // EXISTS
    // =====================================================

    public boolean existsByUid(String packUid) {

        if (packUid == null || packUid.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.ContentPacks.TABLE,
                new String[]{DatabaseContract.ContentPacks.ID},
                DatabaseContract.ContentPacks.PACK_UID + " = ?",
                new String[]{packUid},
                null,
                null,
                null
        )) {

            return cursor.moveToFirst();
        }
    }

    // =====================================================
    // COUNT
    // =====================================================

    public int countPacks() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM "
                        + DatabaseContract.ContentPacks.TABLE,
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // CONTENT VALUES
    // =====================================================

    private ContentValues createValues(ContentPack pack) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ContentPacks.PACK_UID,
                pack.getPackUid()
        );

        values.put(
                DatabaseContract.ContentPacks.TITLE,
                pack.getTitle()
        );

        values.put(
                DatabaseContract.ContentPacks.LANGUAGE,
                pack.getLanguage()
        );

        values.put(
                DatabaseContract.ContentPacks.VERSION,
                pack.getVersion()
        );

        values.put(
                DatabaseContract.ContentPacks.DESCRIPTION,
                pack.getDescription()
        );

        values.put(
                DatabaseContract.ContentPacks.CHECKSUM,
                pack.getChecksum()
        );

        values.put(
                DatabaseContract.ContentPacks.CREATED_AT,
                pack.getCreatedAt()
        );

        return values;
    }

    // =====================================================
    // CURSOR MAPPING
    // =====================================================

    private ContentPack readPack(Cursor cursor) {

        ContentPack pack = new ContentPack(
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ContentPacks.PACK_UID
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ContentPacks.TITLE
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ContentPacks.LANGUAGE
                )),
                cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ContentPacks.VERSION
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ContentPacks.DESCRIPTION
                )),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ContentPacks.CHECKSUM
                ))
        );

        pack.setId(cursor.getLong(cursor.getColumnIndexOrThrow(
                DatabaseContract.ContentPacks.ID
        )));

        pack.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseContract.ContentPacks.CREATED_AT
        )));

        return pack;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validatePack(ContentPack pack) {

        if (pack == null) {
            throw new IllegalArgumentException("Pack cannot be null");
        }

        if (pack.getPackUid() == null
                || pack.getPackUid().trim().isEmpty()) {
            throw new IllegalArgumentException("Pack UID cannot be empty");
        }

        if (pack.getTitle() == null
                || pack.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Pack title cannot be empty");
        }

        if (pack.getLanguage() == null
                || pack.getLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("Pack language cannot be empty");
        }

        if (pack.getVersion() <= 0) {
            throw new IllegalArgumentException("Pack version must be positive");
        }

        if (pack.getCreatedAt() == null
                || pack.getCreatedAt().trim().isEmpty()) {
            throw new IllegalArgumentException("Creation date cannot be empty");
        }
    }

    // =====================================================
    // CLOSE
    // =====================================================

    public void close() {
        databaseHelper.close();
    }
}
