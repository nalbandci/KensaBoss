package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.TrainingSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrainingSessionRepository implements AutoCloseable {

    private final DatabaseHelper databaseHelper;

    public TrainingSessionRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // =====================================================
    // INSERT
    // =====================================================

    /**
     * Legacy method kept for compatibility with existing code.
     *
     * This method creates only the session row. It does not store the
     * individual question IDs. New training-session code should use
     * insertSession(session, questionIds).
     */
    public long insertSession(TrainingSession session) {

        validateSession(session);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = createContentValues(session);

        long id = db.insert(
                DatabaseContract.TrainingSessions.TABLE,
                null,
                values
        );

        if (id != -1) {
            session.setId(id);
        }

        return id;
    }

    /**
     * Creates a training session and stores all its question IDs in one
     * database transaction.
     *
     * The number of supplied IDs must equal session.getTotalQuestions().
     * Duplicate or invalid IDs are rejected before writing to the database.
     *
     * If inserting the session or any question link fails, the entire
     * transaction is rolled back.
     */
    public long insertSession(
            TrainingSession session,
            List<Long> questionIds
    ) {

        validateSession(session);
        validateQuestionIds(session, questionIds);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long sessionId = -1;

        db.beginTransaction();

        try {
            ContentValues sessionValues = createContentValues(session);

            sessionId = db.insert(
                    DatabaseContract.TrainingSessions.TABLE,
                    null,
                    sessionValues
            );

            if (sessionId == -1) {
                throw new IllegalStateException(
                        "Could not create training session"
                );
            }

            for (int i = 0; i < questionIds.size(); i++) {
                ContentValues questionValues = new ContentValues();

                questionValues.put(
                        DatabaseContract.SessionQuestions.SESSION_ID,
                        sessionId
                );

                questionValues.put(
                        DatabaseContract.SessionQuestions.QUESTION_ID,
                        questionIds.get(i)
                );

                questionValues.put(
                        DatabaseContract.SessionQuestions.SORT_ORDER,
                        i
                );

                long result = db.insert(
                        DatabaseContract.SessionQuestions.TABLE,
                        null,
                        questionValues
                );

                if (result == -1) {
                    throw new IllegalStateException(
                            "Could not add question to training session: "
                                    + questionIds.get(i)
                    );
                }
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        // Update the model only after the transaction has committed.
        session.setId(sessionId);

        return sessionId;
    }

    private void validateQuestionIds(
            TrainingSession session,
            List<Long> questionIds
    ) {

        if (questionIds == null) {
            throw new IllegalArgumentException(
                    "Question IDs cannot be null"
            );
        }

        if (questionIds.size() != session.getTotalQuestions()) {
            throw new IllegalArgumentException(
                    "Question ID count must equal total questions"
            );
        }

        Set<Long> uniqueIds = new HashSet<>();

        for (Long questionId : questionIds) {

            if (questionId == null || questionId <= 0) {
                throw new IllegalArgumentException(
                        "Question IDs must be positive"
                );
            }

            if (!uniqueIds.add(questionId)) {
                throw new IllegalArgumentException(
                        "Duplicate question ID: " + questionId
                );
            }
        }
    }

    // =====================================================
    // GET QUESTION IDS FOR SESSION
    // =====================================================

    /**
     * Returns the question IDs belonging to a session in their saved order.
     */
    public List<Long> getQuestionIdsForSession(long sessionId) {

        if (sessionId <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        List<Long> questionIds = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] columns = {
                DatabaseContract.SessionQuestions.QUESTION_ID
        };

        String selection =
                DatabaseContract.SessionQuestions.SESSION_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(sessionId)
        };

        String orderBy =
                DatabaseContract.SessionQuestions.SORT_ORDER + " ASC";

        try (Cursor cursor = db.query(
                DatabaseContract.SessionQuestions.TABLE,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        )) {

            while (cursor.moveToNext()) {
                questionIds.add(cursor.getLong(0));
            }
        }

        return questionIds;
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    public TrainingSession getSessionById(long id) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.TrainingSessions.TABLE,
                null,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return readSessionFromCursor(cursor);
            }
        }

        return null;
    }

    // =====================================================
    // GET ALL
    // =====================================================

    public List<TrainingSession> getAllSessions() {

        List<TrainingSession> sessions = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.TrainingSessions.TABLE,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.TrainingSessions.ID + " DESC"
        )) {

            while (cursor.moveToNext()) {
                sessions.add(readSessionFromCursor(cursor));
            }
        }

        return sessions;
    }

    // =====================================================
    // GET BY STATUS
    // =====================================================

    public List<TrainingSession> getSessionsByStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        List<TrainingSession> sessions = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.TrainingSessions.TABLE,
                null,
                DatabaseContract.TrainingSessions.STATUS + " = ?",
                new String[]{status},
                null,
                null,
                DatabaseContract.TrainingSessions.ID + " DESC"
        )) {

            while (cursor.moveToNext()) {
                sessions.add(readSessionFromCursor(cursor));
            }
        }

        return sessions;
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public int updateSession(TrainingSession session) {

        validateSession(session);

        if (session.getId() <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = createContentValues(session);

        return db.update(
                DatabaseContract.TrainingSessions.TABLE,
                values,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(session.getId())}
        );
    }

    // =====================================================
    // UPDATE RESULTS
    // =====================================================

    public int updateResults(
            long sessionId,
            int correctCount,
            int incorrectCount
    ) {

        if (sessionId <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        if (correctCount < 0 || incorrectCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.TrainingSessions.CORRECT_COUNT,
                correctCount
        );

        values.put(
                DatabaseContract.TrainingSessions.INCORRECT_COUNT,
                incorrectCount
        );

        return db.update(
                DatabaseContract.TrainingSessions.TABLE,
                values,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    // =====================================================
    // FINISH SESSION
    // =====================================================

    public int finishSession(long sessionId) {

        if (sessionId <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.TrainingSessions.STATUS,
                TrainingSession.STATUS_COMPLETED
        );

        values.put(
                DatabaseContract.TrainingSessions.FINISHED_AT,
                getCurrentDateTime()
        );

        return db.update(
                DatabaseContract.TrainingSessions.TABLE,
                values,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    // =====================================================
    // CANCEL SESSION
    // =====================================================

    public int cancelSession(long sessionId) {

        if (sessionId <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.TrainingSessions.STATUS,
                TrainingSession.STATUS_CANCELLED
        );

        values.put(
                DatabaseContract.TrainingSessions.FINISHED_AT,
                getCurrentDateTime()
        );

        return db.update(
                DatabaseContract.TrainingSessions.TABLE,
                values,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    // =====================================================
    // DELETE
    // =====================================================

    public int deleteSessionById(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.TrainingSessions.TABLE,
                DatabaseContract.TrainingSessions.ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // =====================================================
    // COUNT
    // =====================================================

    public int countSessions() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM "
                + DatabaseContract.TrainingSessions.TABLE;

        try (Cursor cursor = db.rawQuery(sql, null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =====================================================
    // CONTENT VALUES
    // =====================================================

    private ContentValues createContentValues(TrainingSession session) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.TrainingSessions.FEEDBACK_MODE,
                session.getFeedbackMode()
        );

        values.put(
                DatabaseContract.TrainingSessions.QUESTION_ORDER,
                session.getQuestionOrder()
        );

        values.put(
                DatabaseContract.TrainingSessions.TOTAL_QUESTIONS,
                session.getTotalQuestions()
        );

        values.put(
                DatabaseContract.TrainingSessions.CORRECT_COUNT,
                session.getCorrectCount()
        );

        values.put(
                DatabaseContract.TrainingSessions.INCORRECT_COUNT,
                session.getIncorrectCount()
        );

        values.put(
                DatabaseContract.TrainingSessions.STATUS,
                session.getStatus()
        );

        values.put(
                DatabaseContract.TrainingSessions.STARTED_AT,
                session.getStartedAt()
        );

        if (session.getFinishedAt() == null) {
            values.putNull(
                    DatabaseContract.TrainingSessions.FINISHED_AT
            );
        } else {
            values.put(
                    DatabaseContract.TrainingSessions.FINISHED_AT,
                    session.getFinishedAt()
            );
        }

        return values;
    }

    // =====================================================
    // READ CURSOR
    // =====================================================

    private TrainingSession readSessionFromCursor(Cursor cursor) {

        int idIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.ID
        );

        int feedbackIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.FEEDBACK_MODE
        );

        int orderIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.QUESTION_ORDER
        );

        int totalIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.TOTAL_QUESTIONS
        );

        int correctIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.CORRECT_COUNT
        );

        int incorrectIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.INCORRECT_COUNT
        );

        int statusIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.STATUS
        );

        int startedIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.STARTED_AT
        );

        int finishedIndex = cursor.getColumnIndexOrThrow(
                DatabaseContract.TrainingSessions.FINISHED_AT
        );

        TrainingSession session = new TrainingSession(
                cursor.getString(feedbackIndex),
                cursor.getString(orderIndex),
                cursor.getInt(totalIndex)
        );

        session.setId(cursor.getLong(idIndex));
        session.setCorrectCount(cursor.getInt(correctIndex));
        session.setIncorrectCount(cursor.getInt(incorrectIndex));
        session.setStatus(cursor.getString(statusIndex));
        session.setStartedAt(cursor.getString(startedIndex));

        if (cursor.isNull(finishedIndex)) {
            session.setFinishedAt(null);
        } else {
            session.setFinishedAt(cursor.getString(finishedIndex));
        }

        return session;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateSession(TrainingSession session) {

        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        if (!TrainingSession.FEEDBACK_IMMEDIATE.equals(session.getFeedbackMode())
                && !TrainingSession.FEEDBACK_DEFERRED.equals(session.getFeedbackMode())) {

            throw new IllegalArgumentException("Invalid feedback mode");
        }

        if (!TrainingSession.ORDER_RANDOM.equals(session.getQuestionOrder())
                && !TrainingSession.ORDER_SEQUENTIAL.equals(session.getQuestionOrder())) {

            throw new IllegalArgumentException("Invalid question order");
        }

        if (session.getTotalQuestions() < 0) {
            throw new IllegalArgumentException("Total questions cannot be negative");
        }

        if (session.getCorrectCount() < 0 || session.getIncorrectCount() < 0) {
            throw new IllegalArgumentException("Results cannot be negative");
        }

        if (session.getStatus() == null || session.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Session status cannot be empty");
        }

        if (session.getStartedAt() == null || session.getStartedAt().trim().isEmpty()) {
            throw new IllegalArgumentException("Start time cannot be empty");
        }
    }

    // =====================================================
    // DATE AND TIME
    // =====================================================

    private String getCurrentDateTime() {

        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        java.util.Locale.US
                );

        return formatter.format(new java.util.Date());
    }

    // =====================================================
    // CLOSE
    // =====================================================

    @Override
    public void close() {
        databaseHelper.close();
    }
}
