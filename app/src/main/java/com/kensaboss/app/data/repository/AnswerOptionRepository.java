
package com.kensaboss.app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;
import com.kensaboss.app.models.AnswerOption;

import java.util.ArrayList;
import java.util.List;

public class AnswerOptionRepository implements AutoCloseable {

    private final DatabaseHelper databaseHelper;

    public AnswerOptionRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long insertAnswerOption(AnswerOption option) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.AnswerOptions.QUESTION_ID, option.getQuestionId());
        values.put(DatabaseContract.AnswerOptions.EXTERNAL_ID, option.getExternalId());
        values.put(DatabaseContract.AnswerOptions.OPTION_TEXT, option.getOptionText());
        values.put(DatabaseContract.AnswerOptions.OPTION_LABEL, option.getOptionLabel());
        values.put(DatabaseContract.AnswerOptions.SORT_ORDER, option.getSortOrder());

        long id = db.insertOrThrow(
                DatabaseContract.AnswerOptions.TABLE,
                null,
                values
        );

        option.setId(id);
        return id;
    }

    public List<AnswerOption> getAnswerOptionsByQuestionId(long questionId) {
        List<AnswerOption> options = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.AnswerOptions.TABLE,
                null,
                DatabaseContract.AnswerOptions.QUESTION_ID + "=?",
                new String[]{String.valueOf(questionId)},
                null,
                null,
                DatabaseContract.AnswerOptions.SORT_ORDER + " ASC"
        )) {
            while (cursor.moveToNext()) {
                options.add(readAnswerOptionFromCursor(cursor));
            }
        }

        return options;
    }

    public AnswerOption getAnswerOptionById(long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DatabaseContract.AnswerOptions.TABLE,
                null,
                DatabaseContract.AnswerOptions.ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return readAnswerOptionFromCursor(cursor);
            }
        }

        return null;
    }

    public AnswerOption getAnswerOptionByExternalId(
            long questionId,
            String externalId
    ) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection =
                DatabaseContract.AnswerOptions.QUESTION_ID + "=? AND " +
                        DatabaseContract.AnswerOptions.EXTERNAL_ID + "=?";

        try (Cursor cursor = db.query(
                DatabaseContract.AnswerOptions.TABLE,
                null,
                selection,
                new String[]{
                        String.valueOf(questionId),
                        externalId
                },
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return readAnswerOptionFromCursor(cursor);
            }
        }

        return null;
    }

    public boolean existsByExternalId(
            long questionId,
            String externalId
    ) {
        return getAnswerOptionByExternalId(questionId, externalId) != null;
    }

    public int updateAnswerOption(AnswerOption option) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.AnswerOptions.QUESTION_ID, option.getQuestionId());
        values.put(DatabaseContract.AnswerOptions.EXTERNAL_ID, option.getExternalId());
        values.put(DatabaseContract.AnswerOptions.OPTION_TEXT, option.getOptionText());
        values.put(DatabaseContract.AnswerOptions.OPTION_LABEL, option.getOptionLabel());
        values.put(DatabaseContract.AnswerOptions.SORT_ORDER, option.getSortOrder());

        return db.update(
                DatabaseContract.AnswerOptions.TABLE,
                values,
                DatabaseContract.AnswerOptions.ID + "=?",
                new String[]{String.valueOf(option.getId())}
        );
    }

    public int deleteAnswerOption(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.delete(
                DatabaseContract.AnswerOptions.TABLE,
                DatabaseContract.AnswerOptions.ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int countAnswerOptions(long questionId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT COUNT(*) FROM " +
                        DatabaseContract.AnswerOptions.TABLE +
                        " WHERE " +
                        DatabaseContract.AnswerOptions.QUESTION_ID + "=?";

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

    private AnswerOption readAnswerOptionFromCursor(Cursor cursor) {
        AnswerOption option = new AnswerOption(
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.AnswerOptions.QUESTION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.AnswerOptions.EXTERNAL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.AnswerOptions.OPTION_TEXT)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseContract.AnswerOptions.OPTION_LABEL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.AnswerOptions.SORT_ORDER))
        );

        option.setId(cursor.getLong(cursor.getColumnIndexOrThrow(
                DatabaseContract.AnswerOptions.ID)));

        return option;
    }

    public void close() {
        databaseHelper.close();
    }
}
