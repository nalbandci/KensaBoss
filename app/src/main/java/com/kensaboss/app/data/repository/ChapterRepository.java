
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.Chapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterRepository {

    private final DatabaseHelper databaseHelper;

    public ChapterRepository(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    // ==========================================
    // 1. СОХРАНЕНИЕ ГЛАВЫ
    // ==========================================

    public long insertChapter(Chapter chapter) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.Chapters.BOOK_PART_ID,
                chapter.getBookPartId()
        );

        values.put(
                DatabaseContract.Chapters.EXTERNAL_ID,
                chapter.getExternalId()
        );

        values.put(
                DatabaseContract.Chapters.TITLE,
                chapter.getTitle()
        );

        values.put(
                DatabaseContract.Chapters.CHAPTER_NUMBER,
                chapter.getChapterNumber()
        );

        values.put(
                DatabaseContract.Chapters.SORT_ORDER,
                chapter.getSortOrder()
        );

        long id = db.insertOrThrow(
                DatabaseContract.Chapters.TABLE,
                null,
                values
        );

        chapter.setId(id);

        return id;
    }

    // ==========================================
    // 2. ПОЛУЧЕНИЕ ГЛАВ ЧАСТИ УЧЕБНИКА
    // ==========================================

    public List<Chapter> getChaptersByBookPartId(long bookPartId) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Chapter> chapters = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.Chapters.TABLE,
                    null,
                    DatabaseContract.Chapters.BOOK_PART_ID + " = ?",
                    new String[]{String.valueOf(bookPartId)},
                    null,
                    null,
                    DatabaseContract.Chapters.SORT_ORDER + " ASC"
            );

            while (cursor.moveToNext()) {
                chapters.add(readChapterFromCursor(cursor));
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return chapters;
    }

    // ==========================================
    // 3. ПОЛУЧЕНИЕ ГЛАВЫ ПО ID
    // ==========================================

    public Chapter getChapterById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.Chapters.TABLE,
                    null,
                    DatabaseContract.Chapters.ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return readChapterFromCursor(cursor);
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

    public Chapter getChapterByExternalId(
            long bookPartId,
            String externalId
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.Chapters.TABLE,
                    null,
                    DatabaseContract.Chapters.BOOK_PART_ID + " = ? AND " +
                            DatabaseContract.Chapters.EXTERNAL_ID + " = ?",
                    new String[]{
                            String.valueOf(bookPartId),
                            externalId
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return readChapterFromCursor(cursor);
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
            long bookPartId,
            String externalId
    ) {

        return getChapterByExternalId(
                bookPartId,
                externalId
        ) != null;
    }

    // ==========================================
    // 6. ОБНОВЛЕНИЕ ГЛАВЫ
    // ==========================================

    public int updateChapter(Chapter chapter) {

        if (chapter.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Chapter ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.Chapters.TITLE,
                chapter.getTitle()
        );

        values.put(
                DatabaseContract.Chapters.CHAPTER_NUMBER,
                chapter.getChapterNumber()
        );

        values.put(
                DatabaseContract.Chapters.SORT_ORDER,
                chapter.getSortOrder()
        );

        return db.update(
                DatabaseContract.Chapters.TABLE,
                values,
                DatabaseContract.Chapters.ID + " = ?",
                new String[]{String.valueOf(chapter.getId())}
        );
    }

    // ==========================================
    // 7. ПРЕОБРАЗОВАНИЕ CURSOR В CHAPTER
    // ==========================================

    private Chapter readChapterFromCursor(Cursor cursor) {

        Chapter chapter = new Chapter(

                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Chapters.BOOK_PART_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Chapters.EXTERNAL_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Chapters.TITLE
                        )
                ),

                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Chapters.CHAPTER_NUMBER
                        )
                ),

                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Chapters.SORT_ORDER
                        )
                )
        );

        chapter.setId(
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Chapters.ID
                        )
                )
        );

        return chapter;
    }

    // ==========================================
    // 8. ЗАКРЫТИЕ РАБОТЫ С БАЗОЙ
    // ==========================================

    public void close() {
        databaseHelper.close();
    }
}
