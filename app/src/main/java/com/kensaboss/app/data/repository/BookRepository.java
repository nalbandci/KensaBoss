
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.importer.PackAssetExtractor;
import com.kensaboss.app.models.Book;
import com.kensaboss.app.models.BookCatalogItem;

import java.util.ArrayList;
import java.util.List;

public class BookRepository {

    private final DatabaseHelper databaseHelper;
    private final Context appContext;

    public BookRepository(Context context) {
        appContext = context.getApplicationContext();
        databaseHelper = new DatabaseHelper(appContext);
    }

    // =====================================================
    // 1. ДОБАВЛЕНИЕ КНИГИ
    // =====================================================

    public long insertBook(Book book) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.Books.PACK_ID, book.getPackId());
        values.put(DatabaseContract.Books.EXTERNAL_ID, book.getExternalId());
        values.put(DatabaseContract.Books.TITLE, book.getTitle());
        values.put(DatabaseContract.Books.AUTHOR, book.getAuthor());
        values.put(DatabaseContract.Books.LANGUAGE, book.getLanguage());
        values.put(DatabaseContract.Books.DESCRIPTION, book.getDescription());
        values.put(DatabaseContract.Books.VERSION, book.getVersion());

        String createdAt = book.getCreatedAt();

        if (createdAt == null || createdAt.trim().isEmpty()) {
            createdAt = String.valueOf(System.currentTimeMillis());
        }

        values.put(DatabaseContract.Books.CREATED_AT, createdAt);

        long id = db.insertOrThrow(
                DatabaseContract.Books.TABLE,
                null,
                values
        );

        book.setId(id);
        book.setCreatedAt(createdAt);

        return id;
    }

    // =====================================================
    // 2. ПОЛУЧЕНИЕ КНИГИ ПО ID
    // =====================================================

    public Book getBookById(long bookId) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection =
                DatabaseContract.Books.ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(bookId)
        };

        try (Cursor cursor = db.query(
                DatabaseContract.Books.TABLE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readBookFromCursor(cursor);
            }
        }

        return null;
    }

    // =====================================================
    // 3. ПОЛУЧЕНИЕ КНИГИ ПО EXTERNAL ID
    // =====================================================

    public Book getBookByExternalId(
            long packId,
            String externalId
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection =
                DatabaseContract.Books.PACK_ID + " = ? AND " +
                        DatabaseContract.Books.EXTERNAL_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(packId),
                externalId
        };

        try (Cursor cursor = db.query(
                DatabaseContract.Books.TABLE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readBookFromCursor(cursor);
            }
        }

        return null;
    }

    // =====================================================
    // 4. ПРОВЕРКА СУЩЕСТВОВАНИЯ КНИГИ
    // =====================================================

    public boolean existsByExternalId(
            long packId,
            String externalId
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection =
                DatabaseContract.Books.PACK_ID + " = ? AND " +
                        DatabaseContract.Books.EXTERNAL_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(packId),
                externalId
        };

        try (Cursor cursor = db.query(
                DatabaseContract.Books.TABLE,
                new String[]{DatabaseContract.Books.ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            return cursor.moveToFirst();
        }
    }

    // =====================================================
    // 5. ОБНОВЛЕНИЕ КНИГИ
    // =====================================================

    public int updateBook(Book book) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.Books.TITLE, book.getTitle());
        values.put(DatabaseContract.Books.AUTHOR, book.getAuthor());
        values.put(DatabaseContract.Books.LANGUAGE, book.getLanguage());
        values.put(DatabaseContract.Books.DESCRIPTION, book.getDescription());
        values.put(DatabaseContract.Books.VERSION, book.getVersion());

        String whereClause =
                DatabaseContract.Books.ID + " = ?";

        String[] whereArgs = {
                String.valueOf(book.getId())
        };

        return db.update(
                DatabaseContract.Books.TABLE,
                values,
                whereClause,
                whereArgs
        );
    }

    // =====================================================
    // 6. ПОЛУЧЕНИЕ ВСЕХ КНИГ
    // =====================================================

    public List<Book> getAllBooks() {

        List<Book> books = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String orderBy =
                DatabaseContract.Books.TITLE + " COLLATE NOCASE ASC";

        try (Cursor cursor = db.query(
                DatabaseContract.Books.TABLE,
                null,
                null,
                null,
                null,
                null,
                orderBy
        )) {

            while (cursor.moveToNext()) {
                books.add(readBookFromCursor(cursor));
            }
        }

        return books;
    }

    // =====================================================
    // 7. ПОЛУЧЕНИЕ КНИГ ПО PACK ID
    // =====================================================

    public List<Book> getBooksByPackId(long packId) {

        List<Book> books = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection =
                DatabaseContract.Books.PACK_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(packId)
        };

        String orderBy =
                DatabaseContract.Books.TITLE + " COLLATE NOCASE ASC";

        try (Cursor cursor = db.query(
                DatabaseContract.Books.TABLE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        )) {

            while (cursor.moveToNext()) {
                books.add(readBookFromCursor(cursor));
            }
        }

        return books;
    }

    // =====================================================
    // 8. ПОЛУЧЕНИЕ КНИГ ДЛЯ КАТАЛОГА
    // =====================================================

    public List<BookCatalogItem> getAllCatalogBooks() {

        List<BookCatalogItem> books = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT " +
                        "b." + DatabaseContract.Books.ID + ", " +
                        "b." + DatabaseContract.Books.TITLE + ", " +
                        "b." + DatabaseContract.Books.AUTHOR + ", " +
                        "b." + DatabaseContract.Books.LANGUAGE + ", " +
                        "b." + DatabaseContract.Books.DESCRIPTION + ", " +

                        "(SELECT COUNT(*) " +
                        "FROM " + DatabaseContract.BookParts.TABLE + " bp " +
                        "WHERE bp." + DatabaseContract.BookParts.BOOK_ID +
                        " = b." + DatabaseContract.Books.ID +
                        ") AS parts_count, " +

                        "(SELECT COUNT(*) " +
                        "FROM " + DatabaseContract.Chapters.TABLE + " c " +
                        "INNER JOIN " + DatabaseContract.BookParts.TABLE +
                        " bp ON bp." + DatabaseContract.BookParts.ID +
                        " = c." + DatabaseContract.Chapters.BOOK_PART_ID +
                        " WHERE bp." + DatabaseContract.BookParts.BOOK_ID +
                        " = b." + DatabaseContract.Books.ID +
                        ") AS chapters_count, " +

                        "(SELECT COUNT(*) " +
                        "FROM " + DatabaseContract.Questions.TABLE + " q " +
                        "WHERE q." + DatabaseContract.Questions.BOOK_ID +
                        " = b." + DatabaseContract.Books.ID +
                        ") AS questions_count " +

                        "FROM " + DatabaseContract.Books.TABLE + " b " +

                        "ORDER BY b." +
                        DatabaseContract.Books.TITLE +
                        " COLLATE NOCASE ASC";

        try (Cursor cursor = db.rawQuery(sql, null)) {

            while (cursor.moveToNext()) {

                BookCatalogItem book = new BookCatalogItem(

                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseContract.Books.ID
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseContract.Books.TITLE
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseContract.Books.AUTHOR
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseContract.Books.LANGUAGE
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseContract.Books.DESCRIPTION
                                )
                        ),

                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("parts_count")
                        ),

                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("chapters_count")
                        ),

                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("questions_count")
                        )
                );

                books.add(book);
            }
        }

        return books;
    }

    // =====================================================
    // 9. УДАЛЕНИЕ КНИГИ С СОХРАНЕНИЕМ ОСТАЛЬНОЙ СТАТИСТИКИ
    // =====================================================

    /**
     * Удаляет книгу и связанные с ней учебные материалы.
     *
     * Ответы на вопросы этой книги удаляются.
     * Ответы на вопросы других книг сохраняются.
     * Статистика затронутых сессий пересчитывается.
     *
     * @return true, если книга была удалена;
     *         false, если книга не найдена.
     */
    public boolean deleteBook(long bookId) {

        if (bookId <= 0) {
            throw new IllegalArgumentException("Invalid book ID");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long packId = -1;
        String packUid = null;

        boolean bookDeleted = false;
        boolean packHasNoBooks = false;

        db.beginTransaction();

        try {

            // Получаем информацию о книге и её пакете.
            // Проверка выполняется внутри транзакции.

            String bookInfoSql =
                    "SELECT b." + DatabaseContract.Books.PACK_ID +
                            ", cp." + DatabaseContract.ContentPacks.PACK_UID +
                            " FROM " + DatabaseContract.Books.TABLE + " b " +
                            " INNER JOIN " + DatabaseContract.ContentPacks.TABLE +
                            " cp ON cp." + DatabaseContract.ContentPacks.ID +
                            " = b." + DatabaseContract.Books.PACK_ID +
                            " WHERE b." + DatabaseContract.Books.ID + " = ?";

            try (Cursor cursor = db.rawQuery(
                    bookInfoSql,
                    new String[]{String.valueOf(bookId)}
            )) {

                if (!cursor.moveToFirst()) {
                    return false;
                }

                packId = cursor.getLong(0);
                packUid = cursor.getString(1);
            }

            // Получаем все сессии, в которых встречались
            // вопросы удаляемой книги.

            List<Long> affectedSessionIds =
                    getAffectedSessionIds(db, bookId);

            // Удаляем только ответы на вопросы этой книги.
            // Ответы на вопросы других книг не затрагиваются.

            String deleteAnswersSql =
                    DatabaseContract.SessionAnswers.QUESTION_ID +
                            " IN (SELECT " +
                            DatabaseContract.Questions.ID +
                            " FROM " + DatabaseContract.Questions.TABLE +
                            " WHERE " + DatabaseContract.Questions.BOOK_ID +
                            " = ?)";

            db.delete(
                    DatabaseContract.SessionAnswers.TABLE,
                    deleteAnswersSql,
                    new String[]{String.valueOf(bookId)}
            );

            // Удаляем книгу.
            // Остальные связанные материалы удаляются
            // через ON DELETE CASCADE.

            int deletedBooks = db.delete(
                    DatabaseContract.Books.TABLE,
                    DatabaseContract.Books.ID + " = ?",
                    new String[]{String.valueOf(bookId)}
            );

            if (deletedBooks != 1) {
                throw new IllegalStateException(
                        "Book could not be deleted"
                );
            }

            bookDeleted = true;

            // Пересчитываем статистику каждой затронутой сессии.

            for (Long sessionId : affectedSessionIds) {
                recalculateSessionStatistics(db, sessionId);
            }

            // Проверяем, остались ли другие книги в этом пакете.

            int remainingBooks = countBooksInPack(db, packId);

            packHasNoBooks = remainingBooks == 0;

            // Если книг больше нет, удаляем запись пакета.
            // История импорта при этом сохраняется.

            if (packHasNoBooks) {

                db.delete(
                        DatabaseContract.ContentPacks.TABLE,
                        DatabaseContract.ContentPacks.ID + " = ?",
                        new String[]{String.valueOf(packId)}
                );
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        // Файлы удаляем только после успешного завершения транзакции.
        // Если в пакете остались книги, общие файлы сохраняются.

        if (bookDeleted && packHasNoBooks && packUid != null) {

            PackAssetExtractor.deletePackAssets(
                    appContext,
                    packUid
            );
        }

        return bookDeleted;
    }

    // =====================================================
    // 10. ПОИСК ЗАТРОНУТЫХ УЧЕБНЫХ СЕССИЙ
    // =====================================================

    private List<Long> getAffectedSessionIds(
            SQLiteDatabase db,
            long bookId
    ) {

        List<Long> sessionIds = new ArrayList<>();

        String sql =
                "SELECT DISTINCT affected.session_id FROM (" +

                        "SELECT sq." +
                        DatabaseContract.SessionQuestions.SESSION_ID +
                        " AS session_id " +
                        "FROM " + DatabaseContract.SessionQuestions.TABLE +
                        " sq " +
                        "INNER JOIN " + DatabaseContract.Questions.TABLE +
                        " q ON q." + DatabaseContract.Questions.ID +
                        " = sq." +
                        DatabaseContract.SessionQuestions.QUESTION_ID +
                        " WHERE q." + DatabaseContract.Questions.BOOK_ID +
                        " = ? " +

                        "UNION " +

                        "SELECT sa." +
                        DatabaseContract.SessionAnswers.SESSION_ID +
                        " AS session_id " +
                        "FROM " + DatabaseContract.SessionAnswers.TABLE +
                        " sa " +
                        "INNER JOIN " + DatabaseContract.Questions.TABLE +
                        " q ON q." + DatabaseContract.Questions.ID +
                        " = sa." +
                        DatabaseContract.SessionAnswers.QUESTION_ID +
                        " WHERE q." + DatabaseContract.Questions.BOOK_ID +
                        " = ?" +

                        ") affected";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{
                        String.valueOf(bookId),
                        String.valueOf(bookId)
                }
        )) {

            while (cursor.moveToNext()) {
                sessionIds.add(cursor.getLong(0));
            }
        }

        return sessionIds;
    }

    // =====================================================
    // 11. ПЕРЕСЧЁТ СТАТИСТИКИ УЧЕБНОЙ СЕССИИ
    // =====================================================

    private void recalculateSessionStatistics(
            SQLiteDatabase db,
            long sessionId
    ) {

        // Считаем оставшиеся вопросы сессии.
        // Удалённые вопросы уже исчезли из session_questions
        // благодаря ON DELETE CASCADE.

        String totalSql =
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.SessionQuestions.TABLE +
                        " WHERE " +
                        DatabaseContract.SessionQuestions.SESSION_ID +
                        " = ?";

        int totalQuestions = 0;

        try (Cursor cursor = db.rawQuery(
                totalSql,
                new String[]{String.valueOf(sessionId)}
        )) {

            if (cursor.moveToFirst()) {
                totalQuestions = cursor.getInt(0);
            }
        }

        // Учитываем только последнюю попытку по каждому вопросу.
        // При равном attempt_number побеждает запись с большим ID.

        String latestAttemptCondition =
                "NOT EXISTS (" +
                        "SELECT 1 FROM " +
                        DatabaseContract.SessionAnswers.TABLE +
                        " newer WHERE " +
                        "newer." +
                        DatabaseContract.SessionAnswers.SESSION_ID +
                        " = sa." +
                        DatabaseContract.SessionAnswers.SESSION_ID +

                        " AND newer." +
                        DatabaseContract.SessionAnswers.QUESTION_ID +
                        " = sa." +
                        DatabaseContract.SessionAnswers.QUESTION_ID +

                        " AND (" +
                        "newer." +
                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER +
                        " > sa." +
                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER +

                        " OR (newer." +
                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER +
                        " = sa." +
                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER +

                        " AND newer." +
                        DatabaseContract.SessionAnswers.ID +
                        " > sa." +
                        DatabaseContract.SessionAnswers.ID +
                        ")" +
                        ")" +
                        ")";

        // Правильные ответы.

        String correctSql =
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.SessionAnswers.TABLE +
                        " sa WHERE sa." +
                        DatabaseContract.SessionAnswers.SESSION_ID +
                        " = ? AND sa." +
                        DatabaseContract.SessionAnswers.IS_CORRECT +
                        " = 1 AND " + latestAttemptCondition;

        int correctCount = countLatestAnswers(
                db,
                correctSql,
                sessionId
        );

        // Неправильные ответы.

        String incorrectSql =
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.SessionAnswers.TABLE +
                        " sa WHERE sa." +
                        DatabaseContract.SessionAnswers.SESSION_ID +
                        " = ? AND sa." +
                        DatabaseContract.SessionAnswers.IS_CORRECT +
                        " = 0 AND " + latestAttemptCondition;

        int incorrectCount = countLatestAnswers(
                db,
                incorrectSql,
                sessionId
        );

        // Записываем обновлённую статистику.

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.TrainingSessions.TOTAL_QUESTIONS,
                totalQuestions
        );

        values.put(
                DatabaseContract.TrainingSessions.CORRECT_COUNT,
                correctCount
        );

        values.put(
                DatabaseContract.TrainingSessions.INCORRECT_COUNT,
                incorrectCount
        );

        db.update(
                DatabaseContract.TrainingSessions.TABLE,
                values,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    // =====================================================
    // 12. ПОДСЧЁТ ПОСЛЕДНИХ ОТВЕТОВ
    // =====================================================

    private int countLatestAnswers(
            SQLiteDatabase db,
            String sql,
            long sessionId
    ) {

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(sessionId)}
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // 13. КОЛИЧЕСТВО КНИГ В ПАКЕТЕ
    // =====================================================

    private int countBooksInPack(
            SQLiteDatabase db,
            long packId
    ) {

        String sql =
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.Books.TABLE +
                        " WHERE " +
                        DatabaseContract.Books.PACK_ID +
                        " = ?";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(packId)}
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // 14. СОЗДАНИЕ BOOK ИЗ CURSOR
    // =====================================================

    private Book readBookFromCursor(Cursor cursor) {

        int packIdIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.Books.PACK_ID
        );

        Long packId = cursor.isNull(packIdIndex)
                ? null
                : cursor.getLong(packIdIndex);

        Book book = new Book(

                packId,

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.EXTERNAL_ID
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.TITLE
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.AUTHOR
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.LANGUAGE
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.DESCRIPTION
                        )
                ),

                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.VERSION
                        )
                )
        );

        book.setId(
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.ID
                        )
                )
        );

        book.setCreatedAt(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.Books.CREATED_AT
                        )
                )
        );

        return book;
    }

    // =====================================================
    // 15. ЗАКРЫТИЕ РЕСУРСОВ
    // =====================================================

    public void close() {
        databaseHelper.close();
    }
}
