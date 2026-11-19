
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.Explanation;

import java.util.ArrayList;
import java.util.List;

public class ExplanationRepository implements AutoCloseable {

    private final DatabaseHelper databaseHelper;

    public ExplanationRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // ==========================================
    // 1. ДОБАВЛЕНИЕ ОБЪЯСНЕНИЯ
    // ==========================================

    public long insertExplanation(Explanation explanation) {

        validateExplanation(explanation);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(explanation);

        long id = db.insert(
                DatabaseContract.Explanations.TABLE,
                null,
                values
        );

        if (id != -1) {
            explanation.setId(id);
        }

        return id;
    }

    // ==========================================
    // 2. ПОЛУЧЕНИЕ ПО ID
    // ==========================================

    public Explanation getExplanationById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Explanations.TABLE,
                null,
                DatabaseContract.Explanations.ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return readExplanationFromCursor(cursor);
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 3. ПОЛУЧЕНИЕ ПО ВОПРОСУ И ЯЗЫКУ
    // ==========================================

    public Explanation getExplanationByQuestionId(
            long questionId,
            String language
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Explanations.TABLE,
                null,
                DatabaseContract.Explanations.QUESTION_ID + " = ? AND " +
                        DatabaseContract.Explanations.LANGUAGE + " = ?",
                new String[]{
                        String.valueOf(questionId),
                        language
                },
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return readExplanationFromCursor(cursor);
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 4. ПОЛУЧЕНИЕ ВСЕХ ОБЪЯСНЕНИЙ
    // ==========================================

    public List<Explanation> getAllExplanations() {

        List<Explanation> explanations = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Explanations.TABLE,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.Explanations.ID + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                explanations.add(readExplanationFromCursor(cursor));
            }

        } finally {
            cursor.close();
        }

        return explanations;
    }

    // ==========================================
    // 5. ПОЛУЧЕНИЕ ОБЪЯСНЕНИЙ ВОПРОСА
    // ==========================================

    public List<Explanation> getExplanationsByQuestionId(
            long questionId
    ) {

        List<Explanation> explanations = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Explanations.TABLE,
                null,
                DatabaseContract.Explanations.QUESTION_ID + " = ?",
                new String[]{String.valueOf(questionId)},
                null,
                null,
                DatabaseContract.Explanations.LANGUAGE + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                explanations.add(readExplanationFromCursor(cursor));
            }

        } finally {
            cursor.close();
        }

        return explanations;
    }

    // ==========================================
    // 6. ПРОВЕРКА СУЩЕСТВОВАНИЯ
    // ==========================================

    public boolean existsByQuestionId(
            long questionId,
            String language
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Explanations.TABLE,
                new String[]{DatabaseContract.Explanations.ID},
                DatabaseContract.Explanations.QUESTION_ID + " = ? AND " +
                        DatabaseContract.Explanations.LANGUAGE + " = ?",
                new String[]{
                        String.valueOf(questionId),
                        language
                },
                null,
                null,
                null,
                "1"
        );

        try {
            return cursor.moveToFirst();

        } finally {
            cursor.close();
        }
    }

    // ==========================================
    // 7. ОБНОВЛЕНИЕ
    // ==========================================

    public int updateExplanation(Explanation explanation) {

        validateExplanation(explanation);

        if (explanation.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Explanation ID must be greater than zero"
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(explanation);

        return db.update(
                DatabaseContract.Explanations.TABLE,
                values,
                DatabaseContract.Explanations.ID + " = ?",
                new String[]{String.valueOf(explanation.getId())}
        );
    }

    // ==========================================
    // 8. УДАЛЕНИЕ ПО ID
    // ==========================================

    public int deleteExplanationById(long id) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.Explanations.TABLE,
                DatabaseContract.Explanations.ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==========================================
    // 9. УДАЛЕНИЕ ПО ВОПРОСУ И ЯЗЫКУ
    // ==========================================

    public int deleteExplanationByQuestionId(
            long questionId,
            String language
    ) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.Explanations.TABLE,
                DatabaseContract.Explanations.QUESTION_ID + " = ? AND " +
                        DatabaseContract.Explanations.LANGUAGE + " = ?",
                new String[]{
                        String.valueOf(questionId),
                        language
                }
        );
    }

    // ==========================================
    // 10. ПОДСЧЁТ ОБЪЯСНЕНИЙ
    // ==========================================

    public int countExplanations() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.Explanations.TABLE,
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
    // 11. СОЗДАНИЕ CONTENT VALUES
    // ==========================================

    private ContentValues createContentValues(
            Explanation explanation
    ) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.Explanations.QUESTION_ID,
                explanation.getQuestionId()
        );

        values.put(
                DatabaseContract.Explanations.LANGUAGE,
                explanation.getLanguage()
        );

        values.put(
                DatabaseContract.Explanations.INTRODUCTION,
                explanation.getIntroduction()
        );

        values.put(
                DatabaseContract.Explanations.DETAILED_TEXT,
                explanation.getDetailedText()
        );

        values.put(
                DatabaseContract.Explanations.SOLUTION_STEPS,
                explanation.getSolutionSteps()
        );

        values.put(
                DatabaseContract.Explanations.ADDITIONAL_NOTES,
                explanation.getAdditionalNotes()
        );

        values.put(
                DatabaseContract.Explanations.VERSION,
                explanation.getVersion()
        );

        return values;
    }

    // ==========================================
    // 12. ЧТЕНИЕ ИЗ CURSOR
    // ==========================================

    private Explanation readExplanationFromCursor(Cursor cursor) {

        Explanation explanation = new Explanation(

                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.QUESTION_ID)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.LANGUAGE)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.INTRODUCTION)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.DETAILED_TEXT)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.SOLUTION_STEPS)),

                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.ADDITIONAL_NOTES)),

                cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.VERSION))
        );

        explanation.setId(
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.Explanations.ID))
        );

        return explanation;
    }

    // ==========================================
    // 13. ПРОВЕРКА ДАННЫХ
    // ==========================================

    private void validateExplanation(Explanation explanation) {

        if (explanation == null) {
            throw new IllegalArgumentException(
                    "Explanation cannot be null"
            );
        }

        if (explanation.getQuestionId() <= 0) {
            throw new IllegalArgumentException(
                    "Question ID must be greater than zero"
            );
        }

        if (explanation.getLanguage() == null ||
                explanation.getLanguage().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Explanation language is required"
            );
        }

        if (explanation.getVersion() <= 0) {
            throw new IllegalArgumentException(
                    "Explanation version must be greater than zero"
            );
        }
    }

    // ==========================================
    // 14. ЗАКРЫТИЕ DATABASE HELPER
    // ==========================================

    public void close() {
        databaseHelper.close();
    }
}
