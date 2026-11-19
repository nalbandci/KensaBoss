
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.SessionAnswer;

import java.util.ArrayList;
import java.util.List;

public class SessionAnswerRepository implements AutoCloseable {

    private final DatabaseHelper databaseHelper;

    public SessionAnswerRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // 1. Сохранение ответа

    public long insertAnswer(SessionAnswer answer) {

        validateAnswer(answer);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = createContentValues(answer);

        long id = db.insertOrThrow(
                DatabaseContract.SessionAnswers.TABLE,
                null,
                values
        );

        answer.setId(id);

        return id;
    }

    // 2. Получение ответа по ID

    public SessionAnswer getAnswerById(long answerId) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.SessionAnswers.TABLE,
                null,
                DatabaseContract.SessionAnswers.ID + " = ?",
                new String[]{String.valueOf(answerId)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readAnswerFromCursor(cursor);
            }
        }

        return null;
    }

    // 3. Получение всех ответов тренировки

    public List<SessionAnswer> getAnswersBySessionId(long sessionId) {

        List<SessionAnswer> answers = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.SessionAnswers.TABLE,
                null,
                DatabaseContract.SessionAnswers.SESSION_ID + " = ?",
                new String[]{String.valueOf(sessionId)},
                null,
                null,
                DatabaseContract.SessionAnswers.QUESTION_ID + " ASC, "
                        + DatabaseContract.SessionAnswers.ATTEMPT_NUMBER + " ASC"
        )) {

            while (cursor.moveToNext()) {
                answers.add(readAnswerFromCursor(cursor));
            }
        }

        return answers;
    }

    // 4. Получение всех попыток по вопросу

    public List<SessionAnswer> getAnswersByQuestionId(long questionId) {

        List<SessionAnswer> answers = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.SessionAnswers.TABLE,
                null,
                DatabaseContract.SessionAnswers.QUESTION_ID + " = ?",
                new String[]{String.valueOf(questionId)},
                null,
                null,
                DatabaseContract.SessionAnswers.ATTEMPT_NUMBER + " ASC"
        )) {

            while (cursor.moveToNext()) {
                answers.add(readAnswerFromCursor(cursor));
            }
        }

        return answers;
    }

    // 5. Получение конкретной попытки

    public SessionAnswer getAnswerByAttempt(
            long sessionId,
            long questionId,
            int attemptNumber
    ) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection =
                DatabaseContract.SessionAnswers.SESSION_ID + " = ? AND "
                        + DatabaseContract.SessionAnswers.QUESTION_ID + " = ? AND "
                        + DatabaseContract.SessionAnswers.ATTEMPT_NUMBER + " = ?";

        String[] selectionArgs = {
                String.valueOf(sessionId),
                String.valueOf(questionId),
                String.valueOf(attemptNumber)
        };

        try (Cursor cursor = db.query(
                DatabaseContract.SessionAnswers.TABLE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readAnswerFromCursor(cursor);
            }
        }

        return null;
    }

    // 6. Обновление ответа

    public int updateAnswer(SessionAnswer answer) {

        if (answer == null || answer.getId() <= 0) {
            throw new IllegalArgumentException("Invalid answer");
        }

        validateAnswer(answer);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.update(
                DatabaseContract.SessionAnswers.TABLE,
                createContentValues(answer),
                DatabaseContract.SessionAnswers.ID + " = ?",
                new String[]{String.valueOf(answer.getId())}
        );
    }

    // 7. Удаление ответа по ID

    public int deleteAnswerById(long answerId) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.SessionAnswers.TABLE,
                DatabaseContract.SessionAnswers.ID + " = ?",
                new String[]{String.valueOf(answerId)}
        );
    }

    // 8. Удаление всех ответов тренировки

    public int deleteAnswersBySessionId(long sessionId) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.SessionAnswers.TABLE,
                DatabaseContract.SessionAnswers.SESSION_ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    // 9. Подсчёт всех ответов

    public int countAnswers() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM "
                + DatabaseContract.SessionAnswers.TABLE;

        try (Cursor cursor = db.rawQuery(sql, null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // 10. Создание ContentValues

    private ContentValues createContentValues(SessionAnswer answer) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.SessionAnswers.SESSION_ID,
                answer.getSessionId()
        );

        values.put(
                DatabaseContract.SessionAnswers.QUESTION_ID,
                answer.getQuestionId()
        );

        values.put(
                DatabaseContract.SessionAnswers.SELECTED_ANSWER,
                answer.getSelectedAnswer()
        );

        if (answer.getIsCorrect() == null) {

            values.putNull(
                    DatabaseContract.SessionAnswers.IS_CORRECT
            );

        } else {

            values.put(
                    DatabaseContract.SessionAnswers.IS_CORRECT,
                    answer.getIsCorrect() ? 1 : 0
            );
        }

        values.put(
                DatabaseContract.SessionAnswers.ANSWERED_AT,
                answer.getAnsweredAt()
        );

        values.put(
                DatabaseContract.SessionAnswers.ATTEMPT_NUMBER,
                answer.getAttemptNumber()
        );

        return values;
    }

    // 11. Чтение ответа из Cursor

    private SessionAnswer readAnswerFromCursor(Cursor cursor) {

        long id = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.SessionAnswers.ID
                )
        );

        long sessionId = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.SessionAnswers.SESSION_ID
                )
        );

        long questionId = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.SessionAnswers.QUESTION_ID
                )
        );

        String selectedAnswer = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.SessionAnswers.SELECTED_ANSWER
                )
        );

        int correctIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.SessionAnswers.IS_CORRECT
        );

        Boolean isCorrect = null;

        if (!cursor.isNull(correctIndex)) {
            isCorrect = cursor.getInt(correctIndex) == 1;
        }

        String answeredAt = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.SessionAnswers.ANSWERED_AT
                )
        );

        int attemptNumber = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER
                )
        );

        SessionAnswer answer = new SessionAnswer(
                sessionId,
                questionId,
                selectedAnswer,
                isCorrect,
                attemptNumber
        );

        answer.setId(id);
        answer.setAnsweredAt(answeredAt);

        return answer;
    }

    // 12. Проверка корректности данных

    private void validateAnswer(SessionAnswer answer) {

        if (answer == null) {
            throw new IllegalArgumentException("Answer cannot be null");
        }

        if (answer.getSessionId() <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        if (answer.getQuestionId() <= 0) {
            throw new IllegalArgumentException("Invalid question ID");
        }

        if (answer.getSelectedAnswer() == null
                || answer.getSelectedAnswer().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Selected answer cannot be empty"
            );
        }

        if (answer.getAttemptNumber() <= 0) {
            throw new IllegalArgumentException(
                    "Attempt number must be greater than zero"
            );
        }

        if (answer.getAnsweredAt() == null
                || answer.getAnsweredAt().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Answer date cannot be empty"
            );
        }
    }

    // 13. Закрытие базы данных

    @Override
    public void close() {
        databaseHelper.close();
    }
}
