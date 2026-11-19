
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionRepository implements AutoCloseable {

    private final DatabaseHelper databaseHelper;

    public QuestionRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // ==========================================
    // 1. ДОБАВЛЕНИЕ ВОПРОСА
    // ==========================================

    public long insertQuestion(Question question) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.Questions.BOOK_ID, question.getBookId());
        values.put(DatabaseContract.Questions.CHAPTER_ID, question.getChapterId());

        if (question.getSectionId() != null) {
            values.put(DatabaseContract.Questions.SECTION_ID, question.getSectionId());
        } else {
            values.putNull(DatabaseContract.Questions.SECTION_ID);
        }

        values.put(DatabaseContract.Questions.EXTERNAL_ID, question.getExternalId());
        values.put(DatabaseContract.Questions.QUESTION_TEXT, question.getQuestionText());
        values.put(DatabaseContract.Questions.QUESTION_TYPE, question.getQuestionType());

        if (question.getOriginalNumber() != null) {
            values.put(DatabaseContract.Questions.ORIGINAL_NUMBER, question.getOriginalNumber());
        } else {
            values.putNull(DatabaseContract.Questions.ORIGINAL_NUMBER);
        }

        if (question.getSourcePage() != null) {
            values.put(DatabaseContract.Questions.SOURCE_PAGE, question.getSourcePage());
        } else {
            values.putNull(DatabaseContract.Questions.SOURCE_PAGE);
        }

        if (question.getDifficulty() != null) {
            values.put(DatabaseContract.Questions.DIFFICULTY, question.getDifficulty());
        } else {
            values.putNull(DatabaseContract.Questions.DIFFICULTY);
        }

        values.put(DatabaseContract.Questions.SORT_ORDER, question.getSortOrder());
        values.put(DatabaseContract.Questions.SOURCE_KEY_STATUS, question.getSourceKeyStatus());

        long id = db.insertOrThrow(
                DatabaseContract.Questions.TABLE,
                null,
                values
        );

        question.setId(id);

        return id;
    }

    // ==========================================
    // 2. ПОЛУЧЕНИЕ ВСЕХ ВОПРОСОВ
    // ==========================================

    public List<Question> getAllQuestions() {

        return getQuestions(null, null);
    }

    // ==========================================
    // 3. ПОЛУЧЕНИЕ ВОПРОСА ПО ID
    // ==========================================

    public Question getQuestionById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Questions.TABLE,
                null,
                DatabaseContract.Questions.ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {

            if (cursor.moveToFirst()) {
                return readQuestionFromCursor(cursor);
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 4. ПОЛУЧЕНИЕ ПО EXTERNAL ID
    // ==========================================

    public Question getQuestionByExternalId(long bookId, String externalId) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Questions.TABLE,
                null,
                DatabaseContract.Questions.BOOK_ID + " = ? AND "
                        + DatabaseContract.Questions.EXTERNAL_ID + " = ?",
                new String[]{
                        String.valueOf(bookId),
                        externalId
                },
                null,
                null,
                null
        );

        try {

            if (cursor.moveToFirst()) {
                return readQuestionFromCursor(cursor);
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 5. ПРОВЕРКА СУЩЕСТВОВАНИЯ
    // ==========================================

    public boolean existsByExternalId(long bookId, String externalId) {

        return getQuestionByExternalId(bookId, externalId) != null;
    }

    // ==========================================
    // 6. ВОПРОСЫ КНИГИ
    // ==========================================

    public List<Question> getQuestionsByBookId(long bookId) {

        return getQuestions(
                DatabaseContract.Questions.BOOK_ID + " = ?",
                new String[]{String.valueOf(bookId)}
        );
    }

    // ==========================================
    // 7. ВОПРОСЫ ГЛАВЫ
    // ==========================================

    public List<Question> getQuestionsByChapterId(long chapterId) {

        return getQuestions(
                DatabaseContract.Questions.CHAPTER_ID + " = ?",
                new String[]{String.valueOf(chapterId)}
        );
    }

    // ==========================================
    // 8. ВОПРОСЫ РАЗДЕЛА
    // ==========================================

    public List<Question> getQuestionsBySectionId(long sectionId) {

        return getQuestions(
                DatabaseContract.Questions.SECTION_ID + " = ?",
                new String[]{String.valueOf(sectionId)}
        );
    }

    // ==========================================
    // 9. ОБЩИЙ МЕТОД ПОЛУЧЕНИЯ СПИСКА
    // ==========================================

    private List<Question> getQuestions(
            String selection,
            String[] selectionArgs
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Question> questions = new ArrayList<>();

        Cursor cursor = db.query(
                DatabaseContract.Questions.TABLE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.Questions.SORT_ORDER + " ASC, "
                        + DatabaseContract.Questions.ID + " ASC"
        );

        try {

            while (cursor.moveToNext()) {
                questions.add(readQuestionFromCursor(cursor));
            }

        } finally {
            cursor.close();
        }

        return questions;
    }

    // ==========================================
    // 10. ОБНОВЛЕНИЕ ВОПРОСА
    // ==========================================

    public int updateQuestion(Question question) {

        if (question.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Question ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseContract.Questions.QUESTION_TEXT, question.getQuestionText());
        values.put(DatabaseContract.Questions.QUESTION_TYPE, question.getQuestionType());

        if (question.getSectionId() != null) {
            values.put(DatabaseContract.Questions.SECTION_ID, question.getSectionId());
        } else {
            values.putNull(DatabaseContract.Questions.SECTION_ID);
        }

        if (question.getOriginalNumber() != null) {
            values.put(DatabaseContract.Questions.ORIGINAL_NUMBER, question.getOriginalNumber());
        } else {
            values.putNull(DatabaseContract.Questions.ORIGINAL_NUMBER);
        }

        if (question.getSourcePage() != null) {
            values.put(DatabaseContract.Questions.SOURCE_PAGE, question.getSourcePage());
        } else {
            values.putNull(DatabaseContract.Questions.SOURCE_PAGE);
        }

        if (question.getDifficulty() != null) {
            values.put(DatabaseContract.Questions.DIFFICULTY, question.getDifficulty());
        } else {
            values.putNull(DatabaseContract.Questions.DIFFICULTY);
        }

        values.put(DatabaseContract.Questions.SORT_ORDER, question.getSortOrder());
        values.put(DatabaseContract.Questions.SOURCE_KEY_STATUS, question.getSourceKeyStatus());

        return db.update(
                DatabaseContract.Questions.TABLE,
                values,
                DatabaseContract.Questions.ID + " = ?",
                new String[]{String.valueOf(question.getId())}
        );
    }

    // ==========================================
    // 11. ЧТЕНИЕ ВОПРОСА ИЗ CURSOR
    // ==========================================

    private Question readQuestionFromCursor(Cursor cursor) {

        int sectionIdIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.Questions.SECTION_ID
        );

        int originalNumberIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.Questions.ORIGINAL_NUMBER
        );

        int sourcePageIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.Questions.SOURCE_PAGE
        );

        int difficultyIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.Questions.DIFFICULTY
        );

        Long sectionId = cursor.isNull(sectionIdIndex)
                ? null
                : cursor.getLong(sectionIdIndex);

        String originalNumber = cursor.isNull(originalNumberIndex)
                ? null
                : cursor.getString(originalNumberIndex);

        Integer sourcePage = cursor.isNull(sourcePageIndex)
                ? null
                : cursor.getInt(sourcePageIndex);

        String difficulty = cursor.isNull(difficultyIndex)
                ? null
                : cursor.getString(difficultyIndex);

        Question question = new Question(

                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.BOOK_ID
                )),

                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.CHAPTER_ID
                )),

                sectionId,

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.EXTERNAL_ID
                )),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.QUESTION_TEXT
                )),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.QUESTION_TYPE
                )),

                originalNumber,
                sourcePage,
                difficulty,

                cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.SORT_ORDER
                )),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.SOURCE_KEY_STATUS
                ))
        );

        question.setId(
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Questions.ID
                ))
        );

        return question;
    }

    // ==========================================
    // 12. ЗАКРЫТИЕ БАЗЫ ДАННЫХ
    // ==========================================

    public void close() {
        databaseHelper.close();
    }
}
