
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.CorrectAnswer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrectAnswerRepository implements AutoCloseable {

    private final DatabaseHelper databaseHelper;

    public CorrectAnswerRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // =====================================================
    // 1. INSERT CORRECT ANSWER
    // =====================================================

    public long insertCorrectAnswer(CorrectAnswer answer) {

        validateAnswer(answer);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(answer);

        long id = db.insertOrThrow(
                DatabaseContract.CorrectAnswers.TABLE,
                null,
                values
        );

        answer.setId(id);

        return id;
    }

    // =====================================================
    // 2. GET ANSWERS BY QUESTION ID
    // =====================================================

    public List<CorrectAnswer> getCorrectAnswersByQuestionId(
            long questionId
    ) {

        if (questionId <= 0) {
            throw new IllegalArgumentException(
                    "Question ID must be greater than zero."
            );
        }

        List<CorrectAnswer> answers = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.CorrectAnswers.TABLE,
                null,
                DatabaseContract.CorrectAnswers.QUESTION_ID + "=?",
                new String[]{String.valueOf(questionId)},
                null,
                null,
                DatabaseContract.CorrectAnswers.SORT_ORDER + " ASC, " +
                        DatabaseContract.CorrectAnswers.ID + " ASC"
        )) {

            while (cursor.moveToNext()) {
                answers.add(readCorrectAnswerFromCursor(cursor));
            }
        }

        return answers;
    }

    // =====================================================
    // 3. GET ANSWER BY ID
    // =====================================================

    public CorrectAnswer getCorrectAnswerById(long id) {

        if (id <= 0) {
            return null;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.CorrectAnswers.TABLE,
                null,
                DatabaseContract.CorrectAnswers.ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readCorrectAnswerFromCursor(cursor);
            }
        }

        return null;
    }

    // =====================================================
    // 4. CHECK EXISTENCE BY ID
    // =====================================================

    public boolean existsById(long id) {
        return getCorrectAnswerById(id) != null;
    }

    // =====================================================
    // 5. UPDATE CORRECT ANSWER
    // =====================================================

    public int updateCorrectAnswer(CorrectAnswer answer) {

        if (answer == null) {
            throw new IllegalArgumentException(
                    "CorrectAnswer cannot be null."
            );
        }

        if (answer.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Answer ID must be greater than zero."
            );
        }

        validateAnswer(answer);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(answer);

        return db.update(
                DatabaseContract.CorrectAnswers.TABLE,
                values,
                DatabaseContract.CorrectAnswers.ID + "=?",
                new String[]{String.valueOf(answer.getId())}
        );
    }

    // =====================================================
    // 6. DELETE ANSWER BY ID
    // =====================================================

    public int deleteCorrectAnswer(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Answer ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.CorrectAnswers.TABLE,
                DatabaseContract.CorrectAnswers.ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // =====================================================
    // 7. DELETE ALL ANSWERS FOR QUESTION
    // =====================================================

    public int deleteCorrectAnswersByQuestionId(long questionId) {

        if (questionId <= 0) {
            throw new IllegalArgumentException(
                    "Question ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.CorrectAnswers.TABLE,
                DatabaseContract.CorrectAnswers.QUESTION_ID + "=?",
                new String[]{String.valueOf(questionId)}
        );
    }

    // =====================================================
    // 8. COUNT ANSWERS
    // =====================================================

    public int countCorrectAnswers(long questionId) {

        if (questionId <= 0) {
            throw new IllegalArgumentException(
                    "Question ID must be greater than zero."
            );
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.CorrectAnswers.TABLE +
                        " WHERE " +
                        DatabaseContract.CorrectAnswers.QUESTION_ID + "=?";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(questionId)}
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // 9. CREATE CONTENT VALUES
    // =====================================================

    /**
     * Преобразует объект CorrectAnswer в ContentValues.
     *
     * Здесь перечислены все поля, которые должны
     * сохраняться в таблице correct_answers.
     */
    private ContentValues createContentValues(CorrectAnswer answer) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.CorrectAnswers.QUESTION_ID,
                answer.getQuestionId()
        );

        if (answer.getOptionId() != null) {
            values.put(
                    DatabaseContract.CorrectAnswers.OPTION_ID,
                    answer.getOptionId()
            );
        } else {
            values.putNull(
                    DatabaseContract.CorrectAnswers.OPTION_ID
            );
        }

        if (answer.getAnswerValue() != null) {
            values.put(
                    DatabaseContract.CorrectAnswers.ANSWER_VALUE,
                    answer.getAnswerValue()
            );
        } else {
            values.putNull(
                    DatabaseContract.CorrectAnswers.ANSWER_VALUE
            );
        }

        // Новый столбец: тип ответа.
        values.put(
                DatabaseContract.CorrectAnswers.ANSWER_TYPE,
                answer.getAnswerType()
        );

        // Новый столбец: порядок ответа.
        values.put(
                DatabaseContract.CorrectAnswers.SORT_ORDER,
                answer.getSortOrder()
        );

        if (answer.getSourceValue() != null) {
            values.put(
                    DatabaseContract.CorrectAnswers.SOURCE_VALUE,
                    answer.getSourceValue()
            );
        } else {
            values.putNull(
                    DatabaseContract.CorrectAnswers.SOURCE_VALUE
            );
        }

        values.put(
                DatabaseContract.CorrectAnswers.VERIFICATION_STATUS,
                answer.getVerificationStatus()
        );

        if (answer.getVerificationNotes() != null) {
            values.put(
                    DatabaseContract.CorrectAnswers.VERIFICATION_NOTES,
                    answer.getVerificationNotes()
            );
        } else {
            values.putNull(
                    DatabaseContract.CorrectAnswers.VERIFICATION_NOTES
            );
        }

        if (answer.getVerifiedAt() != null) {
            values.put(
                    DatabaseContract.CorrectAnswers.VERIFIED_AT,
                    answer.getVerifiedAt()
            );
        } else {
            values.putNull(
                    DatabaseContract.CorrectAnswers.VERIFIED_AT
            );
        }

        return values;
    }

    // =====================================================
    // 10. READ ANSWER FROM CURSOR
    // =====================================================

    private CorrectAnswer readCorrectAnswerFromCursor(
            Cursor cursor
    ) {

        long questionId = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.CorrectAnswers.QUESTION_ID
                )
        );

        int optionIdIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.CorrectAnswers.OPTION_ID
        );

        int answerValueIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.CorrectAnswers.ANSWER_VALUE
        );

        Long optionId = cursor.isNull(optionIdIndex)
                ? null
                : cursor.getLong(optionIdIndex);

        String answerValue = cursor.isNull(answerValueIndex)
                ? null
                : cursor.getString(answerValueIndex);

        // Теперь тип читается непосредственно из базы.
        String answerType = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.CorrectAnswers.ANSWER_TYPE
                )
        );

        // Теперь порядок также читается из базы.
        int sortOrder = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.CorrectAnswers.SORT_ORDER
                )
        );

        CorrectAnswer answer = new CorrectAnswer(
                questionId,
                optionId,
                answerValue,
                answerType,
                sortOrder
        );

        answer.setId(
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.CorrectAnswers.ID
                        )
                )
        );

        answer.setSourceValue(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.CorrectAnswers.SOURCE_VALUE
                        )
                )
        );

        answer.setVerificationStatus(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.CorrectAnswers.VERIFICATION_STATUS
                        )
                )
        );

        answer.setVerificationNotes(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.CorrectAnswers.VERIFICATION_NOTES
                        )
                )
        );

        answer.setVerifiedAt(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseContract.CorrectAnswers.VERIFIED_AT
                        )
                )
        );

        return answer;
    }

    // =====================================================
    // 11. VALIDATE ANSWER
    // =====================================================

    private void validateAnswer(CorrectAnswer answer) {

        if (answer == null) {
            throw new IllegalArgumentException(
                    "CorrectAnswer cannot be null."
            );
        }

        if (answer.getQuestionId() <= 0) {
            throw new IllegalArgumentException(
                    "Question ID must be greater than zero."
            );
        }

        if (answer.getSortOrder() < 0) {
            throw new IllegalArgumentException(
                    "Sort order cannot be negative."
            );
        }

        String answerType = answer.getAnswerType();

        if (answerType == null || answerType.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Answer type is required."
            );
        }

        boolean hasOption = answer.getOptionId() != null;
        boolean hasValue = answer.getAnswerValue() != null;

        if (hasOption == hasValue) {
            throw new IllegalArgumentException(
                    "Exactly one of optionId or answerValue must be provided."
            );
        }

        // Проверка типа OPTION.
        if (CorrectAnswer.TYPE_OPTION.equals(answerType)) {

            if (!hasOption || hasValue) {
                throw new IllegalArgumentException(
                        "OPTION answer must have optionId and no answerValue."
                );
            }

            if (answer.getOptionId() <= 0) {
                throw new IllegalArgumentException(
                        "Option ID must be greater than zero."
                );
            }

        }

        // Проверка типа NUMERIC.
        else if (CorrectAnswer.TYPE_NUMERIC.equals(answerType)) {

            if (hasOption || !hasValue) {
                throw new IllegalArgumentException(
                        "NUMERIC answer must have answerValue and no optionId."
                );
            }

            validateNumericValue(answer.getAnswerValue());
        }

        // Проверка типа TEXT.
        else if (CorrectAnswer.TYPE_TEXT.equals(answerType)) {

            if (hasOption || !hasValue) {
                throw new IllegalArgumentException(
                        "TEXT answer must have answerValue and no optionId."
                );
            }

            if (answer.getAnswerValue().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "TEXT answer cannot be empty."
                );
            }

        }

        // Неизвестный тип ответа.
        else {
            throw new IllegalArgumentException(
                    "Unsupported answer type: " + answerType
            );
        }

        validateVerificationStatus(answer.getVerificationStatus());
    }

    // =====================================================
    // 12. VALIDATE NUMERIC VALUE
    // =====================================================

    /**
     * Проверяет, что значение действительно является числом.
     *
     * Поддерживает целые, десятичные, отрицательные числа
     * и десятичную запятую, например 1,5.
     */
    private void validateNumericValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Numeric answer cannot be empty."
            );
        }

        String normalized = value.trim();

        // Разрешаем одну десятичную запятую,
        // если десятичная точка не используется.
        if (normalized.contains(",") && !normalized.contains(".")) {

            if (normalized.indexOf(',') != normalized.lastIndexOf(',')) {
                throw new IllegalArgumentException(
                        "Invalid numeric answer: " + value
                );
            }

            normalized = normalized.replace(',', '.');
        }

        try {
            new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid numeric answer: " + value,
                    exception
            );
        }
    }

    // =====================================================
    // 13. VALIDATE VERIFICATION STATUS
    // =====================================================

    private void validateVerificationStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Verification status is required."
            );
        }

        List<String> allowedStatuses = Arrays.asList(
                CorrectAnswer.STATUS_NOT_VERIFIED,
                CorrectAnswer.STATUS_VERIFIED,
                CorrectAnswer.STATUS_AMBIGUOUS,
                CorrectAnswer.STATUS_CONFLICTING,
                CorrectAnswer.STATUS_PRINTING_ERROR
        );

        if (!allowedStatuses.contains(status)) {
            throw new IllegalArgumentException(
                    "Unsupported verification status: " + status
            );
        }
    }

    // =====================================================
    // 14. CLOSE DATABASE
    // =====================================================

    public void close() {
        databaseHelper.close();
    }
}
