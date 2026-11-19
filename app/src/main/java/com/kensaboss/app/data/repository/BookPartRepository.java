
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.BookPart;

import java.util.ArrayList;
import java.util.List;

public class BookPartRepository {

    private final DatabaseHelper databaseHelper;

    public BookPartRepository(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    // ==========================================
    // 1. СОХРАНЕНИЕ ЧАСТИ УЧЕБНИКА
    // ==========================================

    public long insertBookPart(BookPart bookPart) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.BookParts.BOOK_ID,
                bookPart.getBookId()
        );

        values.put(
                DatabaseContract.BookParts.EXTERNAL_ID,
                bookPart.getExternalId()
        );

        values.put(
                DatabaseContract.BookParts.TITLE,
                bookPart.getTitle()
        );

        values.put(
                DatabaseContract.BookParts.PART_NUMBER,
                bookPart.getPartNumber()
        );

        values.put(
                DatabaseContract.BookParts.SORT_ORDER,
                bookPart.getSortOrder()
        );

        long id = db.insertOrThrow(
                DatabaseContract.BookParts.TABLE,
                null,
                values
        );

        bookPart.setId(id);

        return id;
    }

    // ==========================================
    // 2. ПОЛУЧЕНИЕ ВСЕХ ЧАСТЕЙ КНИГИ
    // ==========================================

    public List<BookPart> getBookPartsByBookId(long bookId) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<BookPart> parts = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.BookParts.TABLE,
                    null,
                    DatabaseContract.BookParts.BOOK_ID + " = ?",
                    new String[]{String.valueOf(bookId)},
                    null,
                    null,
                    DatabaseContract.BookParts.SORT_ORDER + " ASC"
            );

            while (cursor.moveToNext()) {
                parts.add(readBookPartFromCursor(cursor));
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return parts;
    }

    // ==========================================
    // 3. ПОЛУЧЕНИЕ ЧАСТИ ПО ID
    // ==========================================

    public BookPart getBookPartById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.BookParts.TABLE,
                    null,
                    DatabaseContract.BookParts.ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return readBookPartFromCursor(cursor);
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

    public BookPart getBookPartByExternalId(
            long bookId,
            String externalId
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    DatabaseContract.BookParts.TABLE,
                    null,
                    DatabaseContract.BookParts.BOOK_ID + " = ? AND " +
                            DatabaseContract.BookParts.EXTERNAL_ID + " = ?",
                    new String[]{
                            String.valueOf(bookId),
                            externalId
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return readBookPartFromCursor(cursor);
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
            long bookId,
            String externalId
    ) {

        return getBookPartByExternalId(
                bookId,
                externalId
        ) != null;
    }

    // ==========================================
    // 6. ОБНОВЛЕНИЕ ЧАСТИ УЧЕБНИКА
    // ==========================================

    public int updateBookPart(BookPart bookPart) {

        if (bookPart.getId() <= 0) {
            throw new IllegalArgumentException(
                    "BookPart ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.BookParts.TITLE,
                bookPart.getTitle()
        );

        values.put(
                DatabaseContract.BookParts.PART_NUMBER,
                bookPart.getPartNumber()
        );

        values.put(
                DatabaseContract.BookParts.SORT_ORDER,
                bookPart.getSortOrder()
        );

        return db.update(
                DatabaseContract.BookParts.TABLE,
                values,
                DatabaseContract.BookParts.ID + " = ?",
                new String[]{String.valueOf(bookPart.getId())}
        );
    }

    // ==========================================
    // 7. ПРЕОБРАЗОВАНИЕ CURSOR В BOOK PART
    // ==========================================

    private BookPart readBookPartFromCursor(Cursor cursor) {

        BookPart bookPart = new BookPart(

                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.BookParts.BOOK_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.BookParts.EXTERNAL_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.BookParts.TITLE
                        )
                ),

                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.BookParts.PART_NUMBER
                        )
                ),

                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.BookParts.SORT_ORDER
                        )
                )
        );

        bookPart.setId(
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.BookParts.ID
                        )
                )
        );

        return bookPart;
    }

    // ==========================================
    // 8. ЗАКРЫТИЕ РАБОТЫ С БАЗОЙ
    // ==========================================

    public void close() {
        databaseHelper.close();
    }
}
