
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.Section;

import java.util.ArrayList;
import java.util.List;

public class SectionRepository {

    private final DatabaseHelper databaseHelper;

    public SectionRepository(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    // ==========================================
    // 1. СОХРАНЕНИЕ РАЗДЕЛА
    // ==========================================

    public long insertSection(Section section) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.Sections.CHAPTER_ID,
                section.getChapterId()
        );

        values.put(
                DatabaseContract.Sections.EXTERNAL_ID,
                section.getExternalId()
        );

        values.put(
                DatabaseContract.Sections.TITLE,
                section.getTitle()
        );

        values.put(
                DatabaseContract.Sections.SORT_ORDER,
                section.getSortOrder()
        );

        long id = db.insertOrThrow(
                DatabaseContract.Sections.TABLE,
                null,
                values
        );

        section.setId(id);

        return id;
    }

    // ==========================================
    // 2. ПОЛУЧЕНИЕ РАЗДЕЛОВ ГЛАВЫ
    // ==========================================

    public List<Section> getSectionsByChapterId(long chapterId) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Section> sections = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.Sections.TABLE,
                    null,
                    DatabaseContract.Sections.CHAPTER_ID + " = ?",
                    new String[]{String.valueOf(chapterId)},
                    null,
                    null,
                    DatabaseContract.Sections.SORT_ORDER + " ASC"
            );

            while (cursor.moveToNext()) {
                sections.add(readSectionFromCursor(cursor));
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return sections;
    }

    // ==========================================
    // 3. ПОЛУЧЕНИЕ РАЗДЕЛА ПО ID
    // ==========================================

    public Section getSectionById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.Sections.TABLE,
                    null,
                    DatabaseContract.Sections.ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return readSectionFromCursor(cursor);
            }

            return null;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==========================================
    // 4. ПОИСК ПО EXTERNAL ID
    // ==========================================

    public Section getSectionByExternalId(
            long chapterId,
            String externalId
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.Sections.TABLE,
                    null,
                    DatabaseContract.Sections.CHAPTER_ID + " = ? AND " +
                            DatabaseContract.Sections.EXTERNAL_ID + " = ?",
                    new String[]{
                            String.valueOf(chapterId),
                            externalId
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return readSectionFromCursor(cursor);
            }

            return null;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==========================================
    // 5. ПРОВЕРКА СУЩЕСТВОВАНИЯ
    // ==========================================

    public boolean existsByExternalId(
            long chapterId,
            String externalId
    ) {

        return getSectionByExternalId(
                chapterId,
                externalId
        ) != null;
    }

    // ==========================================
    // 6. ОБНОВЛЕНИЕ РАЗДЕЛА
    // ==========================================

    public int updateSection(Section section) {

        if (section.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Section ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.Sections.TITLE,
                section.getTitle()
        );

        values.put(
                DatabaseContract.Sections.SORT_ORDER,
                section.getSortOrder()
        );

        return db.update(
                DatabaseContract.Sections.TABLE,
                values,
                DatabaseContract.Sections.ID + " = ?",
                new String[]{String.valueOf(section.getId())}
        );
    }

    // ==========================================
    // 7. ПРЕОБРАЗОВАНИЕ CURSOR В SECTION
    // ==========================================

    private Section readSectionFromCursor(Cursor cursor) {

        Section section = new Section(

                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Sections.CHAPTER_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Sections.EXTERNAL_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Sections.TITLE
                        )
                ),

                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Sections.SORT_ORDER
                        )
                )
        );

        section.setId(
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Sections.ID
                        )
                )
        );

        return section;
    }

    // ==========================================
    // 8. ЗАКРЫТИЕ РАБОТЫ С БАЗОЙ
    // ==========================================

    public void close() {
        databaseHelper.close();
    }
}
