
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.ExplanationAsset;

import java.util.ArrayList;
import java.util.List;

public class ExplanationAssetRepository {

    private final DatabaseHelper databaseHelper;

    public ExplanationAssetRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // ==========================================
    // 1. ДОБАВЛЕНИЕ МАТЕРИАЛА
    // ==========================================

    public long insertAsset(ExplanationAsset asset) {

        validateAsset(asset);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(asset);

        long id = db.insert(
                DatabaseContract.ExplanationAssets.TABLE,
                null,
                values
        );

        if (id != -1) {
            asset.setId(id);
        }

        return id;
    }

    // ==========================================
    // 2. ПОЛУЧЕНИЕ ПО ID
    // ==========================================

    public ExplanationAsset getAssetById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.ExplanationAssets.TABLE,
                null,
                DatabaseContract.ExplanationAssets.ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return readAssetFromCursor(cursor);
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 3. ПОЛУЧЕНИЕ МАТЕРИАЛОВ ОБЪЯСНЕНИЯ
    // ==========================================

    public List<ExplanationAsset> getAssetsByExplanationId(
            long explanationId
    ) {

        List<ExplanationAsset> assets = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.ExplanationAssets.TABLE,
                null,
                DatabaseContract.ExplanationAssets.EXPLANATION_ID + " = ?",
                new String[]{String.valueOf(explanationId)},
                null,
                null,
                DatabaseContract.ExplanationAssets.SORT_ORDER + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                assets.add(readAssetFromCursor(cursor));
            }

        } finally {
            cursor.close();
        }

        return assets;
    }

    // ==========================================
    // 4. ПОЛУЧЕНИЕ МАТЕРИАЛОВ ПО ТИПУ
    // ==========================================

    public List<ExplanationAsset> getAssetsByType(
            long explanationId,
            String assetType
    ) {

        List<ExplanationAsset> assets = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.ExplanationAssets.TABLE,
                null,
                DatabaseContract.ExplanationAssets.EXPLANATION_ID +
                        " = ? AND " +
                        DatabaseContract.ExplanationAssets.ASSET_TYPE +
                        " = ?",
                new String[]{
                        String.valueOf(explanationId),
                        assetType
                },
                null,
                null,
                DatabaseContract.ExplanationAssets.SORT_ORDER + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                assets.add(readAssetFromCursor(cursor));
            }

        } finally {
            cursor.close();
        }

        return assets;
    }

    // ==========================================
    // 5. ОБНОВЛЕНИЕ МАТЕРИАЛА
    // ==========================================

    public int updateAsset(ExplanationAsset asset) {

        validateAsset(asset);

        if (asset.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Asset ID must be greater than zero"
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(asset);

        return db.update(
                DatabaseContract.ExplanationAssets.TABLE,
                values,
                DatabaseContract.ExplanationAssets.ID + " = ?",
                new String[]{String.valueOf(asset.getId())}
        );
    }

    // ==========================================
    // 6. УДАЛЕНИЕ ПО ID
    // ==========================================

    public int deleteAssetById(long id) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.ExplanationAssets.TABLE,
                DatabaseContract.ExplanationAssets.ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==========================================
    // 7. УДАЛЕНИЕ МАТЕРИАЛОВ ОБЪЯСНЕНИЯ
    // ==========================================

    public int deleteAssetsByExplanationId(long explanationId) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.ExplanationAssets.TABLE,
                DatabaseContract.ExplanationAssets.EXPLANATION_ID + " = ?",
                new String[]{String.valueOf(explanationId)}
        );
    }

    // ==========================================
    // 8. ПОДСЧЁТ МАТЕРИАЛОВ
    // ==========================================

    public int countAssets() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.ExplanationAssets.TABLE,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            return 0;

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 9. СОЗДАНИЕ CONTENT VALUES
    // ==========================================

    private ContentValues createContentValues(
            ExplanationAsset asset
    ) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ExplanationAssets.EXPLANATION_ID,
                asset.getExplanationId()
        );

        values.put(
                DatabaseContract.ExplanationAssets.ASSET_TYPE,
                asset.getAssetType()
        );

        values.put(
                DatabaseContract.ExplanationAssets.FILE_PATH,
                asset.getFilePath()
        );

        values.put(
                DatabaseContract.ExplanationAssets.DESCRIPTION,
                asset.getDescription()
        );

        values.put(
                DatabaseContract.ExplanationAssets.SORT_ORDER,
                asset.getSortOrder()
        );

        return values;
    }

    // ==========================================
    // 10. ЧТЕНИЕ ИЗ CURSOR
    // ==========================================

    private ExplanationAsset readAssetFromCursor(Cursor cursor) {

        ExplanationAsset asset = new ExplanationAsset(

                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.EXPLANATION_ID)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.ASSET_TYPE)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.FILE_PATH)),

                cursor.isNull(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.DESCRIPTION))
                        ? null
                        : cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.DESCRIPTION)),

                cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.SORT_ORDER))
        );

        asset.setId(
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ExplanationAssets.ID))
        );

        return asset;
    }

    // ==========================================
    // 11. ПРОВЕРКА ДАННЫХ
    // ==========================================

    private void validateAsset(ExplanationAsset asset) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Asset cannot be null"
            );
        }

        if (asset.getExplanationId() <= 0) {
            throw new IllegalArgumentException(
                    "Explanation ID must be greater than zero"
            );
        }

        if (asset.getAssetType() == null ||
                asset.getAssetType().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Asset type is required"
            );
        }

        if (asset.getFilePath() == null ||
                asset.getFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "File path is required"
            );
        }

        if (asset.getSortOrder() < 0) {
            throw new IllegalArgumentException(
                    "Sort order cannot be negative"
            );
        }
    }

    // ==========================================
    // 12. ЗАКРЫТИЕ DATABASE HELPER
    // ==========================================

    public void close() {
        databaseHelper.close();
    }
}
