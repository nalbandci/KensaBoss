package com.kensaboss.app.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kensaboss.db";

    // Версия 5: добавлена таблица session_questions,
    // которая хранит полный список вопросов каждой тренировки.
    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // =====================================================
    // CREATE DATABASE
    // =====================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        createContentPacksTable(db);
        createBooksTable(db);
        createBookPartsTable(db);
        createChaptersTable(db);
        createSectionsTable(db);

        createQuestionsTable(db);
        createAnswerOptionsTable(db);
        createCorrectAnswersTable(db);

        createExplanationsTable(db);
        createExplanationAssetsTable(db);

        createTrainingSessionsTable(db);
        createSessionAnswersTable(db);
        createSessionQuestionsTable(db);

        createImportHistoryTable(db);
        createImportErrorsTable(db);

        createIndexes(db);
        createQuestionValidationTriggers(db);
    }

    // =====================================================
    // 1. CONTENT PACKS
    // =====================================================

    private void createContentPacksTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.ContentPacks.TABLE + " (" +

                        DatabaseContract.ContentPacks.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.ContentPacks.PACK_UID +
                        " TEXT NOT NULL UNIQUE, " +

                        DatabaseContract.ContentPacks.TITLE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ContentPacks.LANGUAGE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ContentPacks.VERSION +
                        " INTEGER NOT NULL DEFAULT 1 CHECK (version > 0), " +

                        DatabaseContract.ContentPacks.DESCRIPTION +
                        " TEXT, " +

                        DatabaseContract.ContentPacks.CHECKSUM +
                        " TEXT, " +

                        DatabaseContract.ContentPacks.CREATED_AT +
                        " TEXT NOT NULL" +

                        ")"
        );
    }

    // =====================================================
    // 2. BOOKS
    // =====================================================

    private void createBooksTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.Books.TABLE + " (" +

                        DatabaseContract.Books.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.Books.PACK_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Books.EXTERNAL_ID +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Books.TITLE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Books.AUTHOR +
                        " TEXT, " +

                        DatabaseContract.Books.LANGUAGE +
                        " TEXT NOT NULL DEFAULT 'hy', " +

                        DatabaseContract.Books.DESCRIPTION +
                        " TEXT, " +

                        DatabaseContract.Books.VERSION +
                        " TEXT NOT NULL DEFAULT '1', " +

                        DatabaseContract.Books.CREATED_AT +
                        " TEXT, " +

                        "UNIQUE (" +
                        DatabaseContract.Books.PACK_ID + ", " +
                        DatabaseContract.Books.EXTERNAL_ID +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Books.PACK_ID +
                        ") REFERENCES " +
                        DatabaseContract.ContentPacks.TABLE +
                        "(" + DatabaseContract.ContentPacks.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 3. BOOK PARTS
    // =====================================================

    private void createBookPartsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.BookParts.TABLE + " (" +

                        DatabaseContract.BookParts.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.BookParts.BOOK_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.BookParts.EXTERNAL_ID +
                        " TEXT NOT NULL, " +

                        DatabaseContract.BookParts.TITLE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.BookParts.PART_NUMBER +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.BookParts.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        "UNIQUE (" +
                        DatabaseContract.BookParts.BOOK_ID + ", " +
                        DatabaseContract.BookParts.EXTERNAL_ID +
                        "), " +

                        "UNIQUE (" +
                        DatabaseContract.BookParts.BOOK_ID + ", " +
                        DatabaseContract.BookParts.PART_NUMBER +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.BookParts.BOOK_ID +
                        ") REFERENCES " +
                        DatabaseContract.Books.TABLE +
                        "(" + DatabaseContract.Books.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 4. CHAPTERS
    // =====================================================

    private void createChaptersTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.Chapters.TABLE + " (" +

                        DatabaseContract.Chapters.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.Chapters.BOOK_PART_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Chapters.EXTERNAL_ID +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Chapters.TITLE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Chapters.CHAPTER_NUMBER +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Chapters.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        "UNIQUE (" +
                        DatabaseContract.Chapters.BOOK_PART_ID + ", " +
                        DatabaseContract.Chapters.EXTERNAL_ID +
                        "), " +

                        "UNIQUE (" +
                        DatabaseContract.Chapters.BOOK_PART_ID + ", " +
                        DatabaseContract.Chapters.CHAPTER_NUMBER +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Chapters.BOOK_PART_ID +
                        ") REFERENCES " +
                        DatabaseContract.BookParts.TABLE +
                        "(" + DatabaseContract.BookParts.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 5. SECTIONS
    // =====================================================

    private void createSectionsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.Sections.TABLE + " (" +

                        DatabaseContract.Sections.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.Sections.CHAPTER_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Sections.EXTERNAL_ID +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Sections.TITLE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Sections.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        "UNIQUE (" +
                        DatabaseContract.Sections.CHAPTER_ID + ", " +
                        DatabaseContract.Sections.EXTERNAL_ID +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Sections.CHAPTER_ID +
                        ") REFERENCES " +
                        DatabaseContract.Chapters.TABLE +
                        "(" + DatabaseContract.Chapters.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 6. QUESTIONS
    // =====================================================

    private void createQuestionsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.Questions.TABLE + " (" +

                        DatabaseContract.Questions.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.Questions.BOOK_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Questions.CHAPTER_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Questions.SECTION_ID +
                        " INTEGER, " +

                        DatabaseContract.Questions.EXTERNAL_ID +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Questions.QUESTION_TEXT +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Questions.QUESTION_TYPE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.Questions.ORIGINAL_NUMBER +
                        " TEXT, " +

                        DatabaseContract.Questions.SOURCE_PAGE +
                        " INTEGER, " +

                        DatabaseContract.Questions.DIFFICULTY +
                        " INTEGER NOT NULL DEFAULT 0 " +
                        "CHECK (difficulty BETWEEN 0 AND 5), " +

                        DatabaseContract.Questions.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        DatabaseContract.Questions.SOURCE_KEY_STATUS +
                        " TEXT NOT NULL DEFAULT 'NOT_CHECKED', " +

                        "UNIQUE (" +
                        DatabaseContract.Questions.BOOK_ID + ", " +
                        DatabaseContract.Questions.EXTERNAL_ID +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Questions.BOOK_ID +
                        ") REFERENCES " +
                        DatabaseContract.Books.TABLE +
                        "(" + DatabaseContract.Books.ID +
                        ") ON DELETE CASCADE, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Questions.CHAPTER_ID +
                        ") REFERENCES " +
                        DatabaseContract.Chapters.TABLE +
                        "(" + DatabaseContract.Chapters.ID +
                        ") ON DELETE CASCADE, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Questions.SECTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Sections.TABLE +
                        "(" + DatabaseContract.Sections.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 7. ANSWER OPTIONS
    // =====================================================

    private void createAnswerOptionsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.AnswerOptions.TABLE + " (" +

                        DatabaseContract.AnswerOptions.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.AnswerOptions.QUESTION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.AnswerOptions.EXTERNAL_ID +
                        " TEXT, " +

                        DatabaseContract.AnswerOptions.OPTION_TEXT +
                        " TEXT NOT NULL, " +

                        DatabaseContract.AnswerOptions.OPTION_LABEL +
                        " TEXT, " +

                        DatabaseContract.AnswerOptions.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        "UNIQUE (" +
                        DatabaseContract.AnswerOptions.QUESTION_ID + ", " +
                        DatabaseContract.AnswerOptions.EXTERNAL_ID +
                        "), " +

                        "UNIQUE (" +
                        DatabaseContract.AnswerOptions.QUESTION_ID + ", " +
                        DatabaseContract.AnswerOptions.ID +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.AnswerOptions.QUESTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 8. CORRECT ANSWERS
    // =====================================================

    private void createCorrectAnswersTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.CorrectAnswers.TABLE + " (" +

                        DatabaseContract.CorrectAnswers.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.CorrectAnswers.QUESTION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.CorrectAnswers.OPTION_ID +
                        " INTEGER, " +

                        DatabaseContract.CorrectAnswers.ANSWER_VALUE +
                        " TEXT, " +

                        DatabaseContract.CorrectAnswers.ANSWER_TYPE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.CorrectAnswers.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0 CHECK (sort_order >= 0), " +

                        DatabaseContract.CorrectAnswers.SOURCE_VALUE +
                        " TEXT, " +

                        DatabaseContract.CorrectAnswers.VERIFICATION_STATUS +
                        " TEXT NOT NULL DEFAULT 'NOT_VERIFIED', " +

                        DatabaseContract.CorrectAnswers.VERIFICATION_NOTES +
                        " TEXT, " +

                        DatabaseContract.CorrectAnswers.VERIFIED_AT +
                        " TEXT, " +

                        "CHECK (" +
                        "(option_id IS NOT NULL AND answer_value IS NULL " +
                        "AND answer_type = 'OPTION') OR " +
                        "(option_id IS NULL AND answer_value IS NOT NULL " +
                        "AND answer_type IN ('NUMERIC', 'TEXT'))" +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.CorrectAnswers.QUESTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.ID +
                        ") ON DELETE CASCADE, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.CorrectAnswers.QUESTION_ID + ", " +
                        DatabaseContract.CorrectAnswers.OPTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.AnswerOptions.TABLE +
                        "(" +
                        DatabaseContract.AnswerOptions.QUESTION_ID + ", " +
                        DatabaseContract.AnswerOptions.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 9. EXPLANATIONS
    // =====================================================

    private void createExplanationsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.Explanations.TABLE + " (" +

                        DatabaseContract.Explanations.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.Explanations.QUESTION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.Explanations.LANGUAGE +
                        " TEXT NOT NULL DEFAULT 'hy', " +

                        DatabaseContract.Explanations.INTRODUCTION +
                        " TEXT, " +

                        DatabaseContract.Explanations.DETAILED_TEXT +
                        " TEXT, " +

                        DatabaseContract.Explanations.SOLUTION_STEPS +
                        " TEXT, " +

                        DatabaseContract.Explanations.ADDITIONAL_NOTES +
                        " TEXT, " +

                        DatabaseContract.Explanations.VERSION +
                        " INTEGER NOT NULL DEFAULT 1, " +

                        "UNIQUE (" +
                        DatabaseContract.Explanations.QUESTION_ID + ", " +
                        DatabaseContract.Explanations.LANGUAGE +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.Explanations.QUESTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 10. EXPLANATION ASSETS
    // =====================================================

    private void createExplanationAssetsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.ExplanationAssets.TABLE + " (" +

                        DatabaseContract.ExplanationAssets.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.ExplanationAssets.EXPLANATION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.ExplanationAssets.ASSET_TYPE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ExplanationAssets.FILE_PATH +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ExplanationAssets.DESCRIPTION +
                        " TEXT, " +

                        DatabaseContract.ExplanationAssets.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.ExplanationAssets.EXPLANATION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Explanations.TABLE +
                        "(" + DatabaseContract.Explanations.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 11. TRAINING SESSIONS
    // =====================================================

    private void createTrainingSessionsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.TrainingSessions.TABLE + " (" +

                        DatabaseContract.TrainingSessions.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.TrainingSessions.FEEDBACK_MODE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.TrainingSessions.QUESTION_ORDER +
                        " TEXT NOT NULL, " +

                        DatabaseContract.TrainingSessions.TOTAL_QUESTIONS +
                        " INTEGER NOT NULL DEFAULT 0 CHECK (total_questions >= 0), " +

                        DatabaseContract.TrainingSessions.CORRECT_COUNT +
                        " INTEGER NOT NULL DEFAULT 0 CHECK (correct_count >= 0), " +

                        DatabaseContract.TrainingSessions.INCORRECT_COUNT +
                        " INTEGER NOT NULL DEFAULT 0 CHECK (incorrect_count >= 0), " +

                        DatabaseContract.TrainingSessions.STATUS +
                        " TEXT NOT NULL DEFAULT 'IN_PROGRESS', " +

                        DatabaseContract.TrainingSessions.STARTED_AT +
                        " TEXT NOT NULL, " +

                        DatabaseContract.TrainingSessions.FINISHED_AT +
                        " TEXT" +

                        ")"
        );
    }

    // =====================================================
    // 12. SESSION ANSWERS
    // =====================================================

    private void createSessionAnswersTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.SessionAnswers.TABLE + " (" +

                        DatabaseContract.SessionAnswers.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.SessionAnswers.SESSION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.SessionAnswers.QUESTION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.SessionAnswers.SELECTED_ANSWER +
                        " TEXT, " +

                        DatabaseContract.SessionAnswers.IS_CORRECT +
                        " INTEGER CHECK (is_correct IN (0, 1) OR is_correct IS NULL), " +

                        DatabaseContract.SessionAnswers.ANSWERED_AT +
                        " TEXT, " +

                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER +
                        " INTEGER NOT NULL DEFAULT 1 CHECK (attempt_number > 0), " +

                        "UNIQUE (" +
                        DatabaseContract.SessionAnswers.SESSION_ID + ", " +
                        DatabaseContract.SessionAnswers.QUESTION_ID + ", " +
                        DatabaseContract.SessionAnswers.ATTEMPT_NUMBER +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.SessionAnswers.SESSION_ID +
                        ") REFERENCES " +
                        DatabaseContract.TrainingSessions.TABLE +
                        "(" + DatabaseContract.TrainingSessions.ID +
                        ") ON DELETE CASCADE, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.SessionAnswers.QUESTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.ID +
                        ") ON DELETE RESTRICT" +

                        ")"
        );
    }

    // =====================================================
    // 13. SESSION QUESTIONS
    // =====================================================

    private void createSessionQuestionsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.SessionQuestions.TABLE + " (" +

                        DatabaseContract.SessionQuestions.SESSION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.SessionQuestions.QUESTION_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.SessionQuestions.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0 CHECK (sort_order >= 0), " +

                        "PRIMARY KEY (" +
                        DatabaseContract.SessionQuestions.SESSION_ID + ", " +
                        DatabaseContract.SessionQuestions.QUESTION_ID +
                        "), " +

                        "FOREIGN KEY (" +
                        DatabaseContract.SessionQuestions.SESSION_ID +
                        ") REFERENCES " +
                        DatabaseContract.TrainingSessions.TABLE +
                        "(" + DatabaseContract.TrainingSessions.ID +
                        ") ON DELETE CASCADE, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.SessionQuestions.QUESTION_ID +
                        ") REFERENCES " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // 14. IMPORT HISTORY
    // =====================================================

    private void createImportHistoryTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.ImportHistory.TABLE + " (" +

                        DatabaseContract.ImportHistory.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.ImportHistory.PACK_UID +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ImportHistory.PACK_VERSION +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.ImportHistory.FILE_NAME +
                        " TEXT, " +

                        DatabaseContract.ImportHistory.CHECKSUM +
                        " TEXT, " +

                        DatabaseContract.ImportHistory.STATUS +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ImportHistory.IMPORTED_COUNT +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        DatabaseContract.ImportHistory.ERROR_COUNT +
                        " INTEGER NOT NULL DEFAULT 0, " +

                        DatabaseContract.ImportHistory.STARTED_AT +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ImportHistory.FINISHED_AT +
                        " TEXT" +

                        ")"
        );
    }

    // =====================================================
    // 15. IMPORT ERRORS
    // =====================================================

    private void createImportErrorsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + DatabaseContract.ImportErrors.TABLE + " (" +

                        DatabaseContract.ImportErrors.ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        DatabaseContract.ImportErrors.IMPORT_ID +
                        " INTEGER NOT NULL, " +

                        DatabaseContract.ImportErrors.ENTITY_TYPE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ImportErrors.EXTERNAL_ID +
                        " TEXT, " +

                        DatabaseContract.ImportErrors.SOURCE_PAGE +
                        " INTEGER, " +

                        DatabaseContract.ImportErrors.SEVERITY +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ImportErrors.MESSAGE +
                        " TEXT NOT NULL, " +

                        DatabaseContract.ImportErrors.CREATED_AT +
                        " TEXT NOT NULL, " +

                        "FOREIGN KEY (" +
                        DatabaseContract.ImportErrors.IMPORT_ID +
                        ") REFERENCES " +
                        DatabaseContract.ImportHistory.TABLE +
                        "(" + DatabaseContract.ImportHistory.ID +
                        ") ON DELETE CASCADE" +

                        ")"
        );
    }

    // =====================================================
    // INDEXES
    // =====================================================

    private void createIndexes(SQLiteDatabase db) {

        db.execSQL(
                "CREATE INDEX idx_books_pack ON " +
                        DatabaseContract.Books.TABLE +
                        "(" + DatabaseContract.Books.PACK_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_parts_book ON " +
                        DatabaseContract.BookParts.TABLE +
                        "(" + DatabaseContract.BookParts.BOOK_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_chapters_part ON " +
                        DatabaseContract.Chapters.TABLE +
                        "(" + DatabaseContract.Chapters.BOOK_PART_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_sections_chapter ON " +
                        DatabaseContract.Sections.TABLE +
                        "(" + DatabaseContract.Sections.CHAPTER_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_questions_book ON " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.BOOK_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_questions_chapter ON " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.CHAPTER_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_questions_section ON " +
                        DatabaseContract.Questions.TABLE +
                        "(" + DatabaseContract.Questions.SECTION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_options_question ON " +
                        DatabaseContract.AnswerOptions.TABLE +
                        "(" + DatabaseContract.AnswerOptions.QUESTION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_answers_question ON " +
                        DatabaseContract.CorrectAnswers.TABLE +
                        "(" + DatabaseContract.CorrectAnswers.QUESTION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_explanations_question ON " +
                        DatabaseContract.Explanations.TABLE +
                        "(" + DatabaseContract.Explanations.QUESTION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_session_answers_session ON " +
                        DatabaseContract.SessionAnswers.TABLE +
                        "(" + DatabaseContract.SessionAnswers.SESSION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_session_answers_question ON " +
                        DatabaseContract.SessionAnswers.TABLE +
                        "(" + DatabaseContract.SessionAnswers.QUESTION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_session_questions_session_order ON " +
                        DatabaseContract.SessionQuestions.TABLE +
                        "(" + DatabaseContract.SessionQuestions.SESSION_ID + ", " +
                        DatabaseContract.SessionQuestions.SORT_ORDER + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_session_questions_question ON " +
                        DatabaseContract.SessionQuestions.TABLE +
                        "(" + DatabaseContract.SessionQuestions.QUESTION_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_import_history_pack ON " +
                        DatabaseContract.ImportHistory.TABLE +
                        "(" + DatabaseContract.ImportHistory.PACK_UID + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_import_errors_import ON " +
                        DatabaseContract.ImportErrors.TABLE +
                        "(" + DatabaseContract.ImportErrors.IMPORT_ID + ")"
        );
    }

    // =====================================================
    // QUESTION HIERARCHY VALIDATION
    // =====================================================

    private void createQuestionValidationTriggers(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TRIGGER validate_question_hierarchy_insert " +
                        "BEFORE INSERT ON " +
                        DatabaseContract.Questions.TABLE +
                        " BEGIN " +

                        "SELECT CASE WHEN NOT EXISTS (" +
                        "SELECT 1 FROM chapters c " +
                        "INNER JOIN book_parts bp ON bp.id = c.book_part_id " +
                        "WHERE c.id = NEW.chapter_id " +
                        "AND bp.book_id = NEW.book_id" +
                        ") THEN RAISE(ABORT, 'Chapter does not belong to book') END; " +

                        "SELECT CASE WHEN NEW.section_id IS NOT NULL " +
                        "AND NOT EXISTS (" +
                        "SELECT 1 FROM sections s " +
                        "WHERE s.id = NEW.section_id " +
                        "AND s.chapter_id = NEW.chapter_id" +
                        ") THEN RAISE(ABORT, 'Section does not belong to chapter') END; " +

                        "END"
        );

        db.execSQL(
                "CREATE TRIGGER validate_question_hierarchy_update " +
                        "BEFORE UPDATE OF book_id, chapter_id, section_id " +
                        "ON " + DatabaseContract.Questions.TABLE +
                        " BEGIN " +

                        "SELECT CASE WHEN NOT EXISTS (" +
                        "SELECT 1 FROM chapters c " +
                        "INNER JOIN book_parts bp ON bp.id = c.book_part_id " +
                        "WHERE c.id = NEW.chapter_id " +
                        "AND bp.book_id = NEW.book_id" +
                        ") THEN RAISE(ABORT, 'Chapter does not belong to book') END; " +

                        "SELECT CASE WHEN NEW.section_id IS NOT NULL " +
                        "AND NOT EXISTS (" +
                        "SELECT 1 FROM sections s " +
                        "WHERE s.id = NEW.section_id " +
                        "AND s.chapter_id = NEW.chapter_id" +
                        ") THEN RAISE(ABORT, 'Section does not belong to chapter') END; " +

                        "END"
        );
    }

    // =====================================================
    // DATABASE UPGRADE
    // =====================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 3) {
            // Старые версии 1 и 2 ранее использовали
            // временный сброс тестовой базы.
            dropVersionOneTables(db);
            onCreate(db);
            return;
        }

        int currentVersion = oldVersion;

        // Сначала выполняем миграцию 3 -> 4, если она необходима.
        if (currentVersion == 3) {
            migrateVersionThreeToFour(db);
            currentVersion = 4;
        }

        // Затем выполняем миграцию 4 -> 5.
        if (currentVersion == 4) {
            migrateVersionFourToFive(db);
            currentVersion = 5;
        }

        if (currentVersion != newVersion) {
            throw new IllegalStateException(
                    "Unsupported database migration: " +
                            oldVersion + " -> " + newVersion
            );
        }
    }

    /**
     * Миграция 3 -> 4.
     *
     * Существующие записи сохраняются.
     * Старые ответы с option_id получают тип OPTION.
     * Старые ответы со значением получают тип TEXT,
     * так как исходный тип достоверно восстановить нельзя.
     */
    private void migrateVersionThreeToFour(SQLiteDatabase db) {

        db.execSQL(
                "ALTER TABLE " +
                        DatabaseContract.CorrectAnswers.TABLE +
                        " ADD COLUMN " +
                        DatabaseContract.CorrectAnswers.ANSWER_TYPE +
                        " TEXT NOT NULL DEFAULT 'TEXT'"
        );

        db.execSQL(
                "ALTER TABLE " +
                        DatabaseContract.CorrectAnswers.TABLE +
                        " ADD COLUMN " +
                        DatabaseContract.CorrectAnswers.SORT_ORDER +
                        " INTEGER NOT NULL DEFAULT 0"
        );

        db.execSQL(
                "UPDATE " +
                        DatabaseContract.CorrectAnswers.TABLE +
                        " SET " +
                        DatabaseContract.CorrectAnswers.ANSWER_TYPE +
                        " = CASE " +
                        "WHEN " +
                        DatabaseContract.CorrectAnswers.OPTION_ID +
                        " IS NOT NULL THEN 'OPTION' " +
                        "ELSE 'TEXT' END"
        );
    }

    /**
     * Миграция 4 -> 5.
     *
     * Создаёт таблицу полного состава вопросов каждой тренировки.
     * Для старых тренировок восстанавливает только те вопросы,
     * по которым уже существует хотя бы один ответ.
     *
     * Неотвеченные вопросы старых тренировок восстановить невозможно,
     * поскольку прежняя версия приложения не сохраняла их список отдельно.
     */
    private void migrateVersionFourToFive(SQLiteDatabase db) {

        createSessionQuestionsTable(db);

        db.execSQL(
                "INSERT OR IGNORE INTO " +
                        DatabaseContract.SessionQuestions.TABLE +
                        " (" +
                        DatabaseContract.SessionQuestions.SESSION_ID + ", " +
                        DatabaseContract.SessionQuestions.QUESTION_ID + ", " +
                        DatabaseContract.SessionQuestions.SORT_ORDER +
                        ") " +
                        "SELECT " +
                        DatabaseContract.SessionAnswers.SESSION_ID + ", " +
                        DatabaseContract.SessionAnswers.QUESTION_ID + ", " +
                        "0 " +
                        "FROM " + DatabaseContract.SessionAnswers.TABLE + " " +
                        "GROUP BY " +
                        DatabaseContract.SessionAnswers.SESSION_ID + ", " +
                        DatabaseContract.SessionAnswers.QUESTION_ID
        );

        db.execSQL(
                "CREATE INDEX idx_session_questions_session_order ON " +
                        DatabaseContract.SessionQuestions.TABLE +
                        "(" + DatabaseContract.SessionQuestions.SESSION_ID + ", " +
                        DatabaseContract.SessionQuestions.SORT_ORDER + ")"
        );

        db.execSQL(
                "CREATE INDEX idx_session_questions_question ON " +
                        DatabaseContract.SessionQuestions.TABLE +
                        "(" + DatabaseContract.SessionQuestions.QUESTION_ID + ")"
        );
    }

    private void dropVersionOneTables(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS import_errors");
        db.execSQL("DROP TABLE IF EXISTS import_history");

        db.execSQL("DROP TABLE IF EXISTS session_answers");
        db.execSQL("DROP TABLE IF EXISTS training_sessions");

        db.execSQL("DROP TABLE IF EXISTS explanation_assets");
        db.execSQL("DROP TABLE IF EXISTS explanations");

        db.execSQL("DROP TABLE IF EXISTS correct_answers");
        db.execSQL("DROP TABLE IF EXISTS corrct_answers");

        db.execSQL("DROP TABLE IF EXISTS answer_options");
        db.execSQL("DROP TABLE IF EXISTS questions");

        db.execSQL("DROP TABLE IF EXISTS sections");
        db.execSQL("DROP TABLE IF EXISTS chapters");
        db.execSQL("DROP TABLE IF EXISTS book_parts");
        db.execSQL("DROP TABLE IF EXISTS books");
        db.execSQL("DROP TABLE IF EXISTS content_packs");
    }

    @Override
    public void onDowngrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        throw new IllegalStateException(
                "Database downgrade is not supported: " +
                        oldVersion + " -> " + newVersion
        );
    }
}

