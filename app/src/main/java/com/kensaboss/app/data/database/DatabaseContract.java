package com.kensaboss.app.data.database;

/**
 * Централизованное описание структуры базы данных KensaBoss.
 *
 * Здесь находятся названия таблиц и столбцов.
 * SQL-запросы и операции с данными находятся в других классах.
 */
public final class DatabaseContract {

    private DatabaseContract() {
        // Запрещаем создание объектов этого класса.
    }

    // =====================================================
    // 1. CONTENT PACKS — ПАКЕТЫ ИМПОРТА
    // =====================================================

    public static final class ContentPacks {

        public static final String TABLE = "content_packs";

        public static final String ID = "id";
        public static final String PACK_UID = "pack_uid";
        public static final String TITLE = "title";
        public static final String LANGUAGE = "language";
        public static final String VERSION = "version";
        public static final String DESCRIPTION = "description";
        public static final String CHECKSUM = "checksum";
        public static final String CREATED_AT = "created_at";
    }

    // =====================================================
    // 2. BOOKS — КНИГИ
    // =====================================================

    public static final class Books {

        public static final String TABLE = "books";

        public static final String ID = "id";
        public static final String PACK_ID = "pack_id";
        public static final String EXTERNAL_ID = "external_id";

        public static final String TITLE = "title";
        public static final String AUTHOR = "author";
        public static final String LANGUAGE = "language";
        public static final String DESCRIPTION = "description";

        public static final String VERSION = "version";
        public static final String CREATED_AT = "created_at";
    }

    // =====================================================
    // 3. BOOK PARTS — ЧАСТИ КНИГ
    // =====================================================

    public static final class BookParts {

        public static final String TABLE = "book_parts";

        public static final String ID = "id";
        public static final String BOOK_ID = "book_id";
        public static final String EXTERNAL_ID = "external_id";

        public static final String TITLE = "title";
        public static final String PART_NUMBER = "part_number";
        public static final String SORT_ORDER = "sort_order";
    }

    // =====================================================
    // 4. CHAPTERS — ГЛАВЫ
    // =====================================================

    public static final class Chapters {

        public static final String TABLE = "chapters";

        public static final String ID = "id";
        public static final String BOOK_PART_ID = "book_part_id";
        public static final String EXTERNAL_ID = "external_id";

        public static final String TITLE = "title";
        public static final String CHAPTER_NUMBER = "chapter_number";
        public static final String SORT_ORDER = "sort_order";
    }

    // =====================================================
    // 5. SECTIONS — ПОДРАЗДЕЛЫ
    // =====================================================

    public static final class Sections {

        public static final String TABLE = "sections";

        public static final String ID = "id";
        public static final String CHAPTER_ID = "chapter_id";
        public static final String EXTERNAL_ID = "external_id";

        public static final String TITLE = "title";
        public static final String SORT_ORDER = "sort_order";
    }

    // =====================================================
    // 6. QUESTIONS — ВОПРОСЫ
    // =====================================================

    public static final class Questions {

        public static final String TABLE = "questions";

        public static final String ID = "id";
        public static final String BOOK_ID = "book_id";
        public static final String CHAPTER_ID = "chapter_id";
        public static final String SECTION_ID = "section_id";

        public static final String EXTERNAL_ID = "external_id";

        public static final String QUESTION_TEXT = "question_text";
        public static final String QUESTION_TYPE = "question_type";

        public static final String ORIGINAL_NUMBER = "original_number";
        public static final String SOURCE_PAGE = "source_page";

        public static final String DIFFICULTY = "difficulty";
        public static final String SORT_ORDER = "sort_order";

        public static final String SOURCE_KEY_STATUS = "source_key_status";
    }

    // =====================================================
    // 7. ANSWER OPTIONS — ВАРИАНТЫ ОТВЕТОВ
    // =====================================================

    public static final class AnswerOptions {

        public static final String TABLE = "answer_options";

        public static final String ID = "id";
        public static final String QUESTION_ID = "question_id";
        public static final String EXTERNAL_ID = "external_id";

        public static final String OPTION_TEXT = "option_text";
        public static final String OPTION_LABEL = "option_label";

        public static final String SORT_ORDER = "sort_order";
    }

    // =====================================================
    // 8. CORRECT ANSWERS — ПРАВИЛЬНЫЕ ОТВЕТЫ
    // =====================================================

    public static final class CorrectAnswers {

        public static final String TABLE = "correct_answers";

        public static final String ID = "id";
        public static final String QUESTION_ID = "question_id";
        public static final String OPTION_ID = "option_id";

        public static final String ANSWER_VALUE = "answer_value";
        public static final String ANSWER_TYPE = "answer_type";
        public static final String SORT_ORDER = "sort_order";

        public static final String SOURCE_VALUE = "source_value";

        public static final String VERIFICATION_STATUS = "verification_status";
        public static final String VERIFICATION_NOTES = "verification_notes";
        public static final String VERIFIED_AT = "verified_at";
    }

    // =====================================================
    // 9. EXPLANATIONS — ОБЪЯСНЕНИЯ
    // =====================================================

    public static final class Explanations {

        public static final String TABLE = "explanations";

        public static final String ID = "id";
        public static final String QUESTION_ID = "question_id";

        public static final String LANGUAGE = "language";

        public static final String INTRODUCTION = "introduction";
        public static final String DETAILED_TEXT = "detailed_text";
        public static final String SOLUTION_STEPS = "solution_steps";
        public static final String ADDITIONAL_NOTES = "additional_notes";

        public static final String VERSION = "version";
    }

    // =====================================================
    // 10. EXPLANATION ASSETS — МЕДИАМАТЕРИАЛЫ
    // =====================================================

    public static final class ExplanationAssets {

        public static final String TABLE = "explanation_assets";

        public static final String ID = "id";
        public static final String EXPLANATION_ID = "explanation_id";

        public static final String ASSET_TYPE = "asset_type";
        public static final String FILE_PATH = "file_path";
        public static final String DESCRIPTION = "description";

        public static final String SORT_ORDER = "sort_order";
    }

    // =====================================================
    // 11. TRAINING SESSIONS — УЧЕБНЫЕ СЕССИИ
    // =====================================================

    public static final class TrainingSessions {

        public static final String TABLE = "training_sessions";

        public static final String ID = "id";

        public static final String FEEDBACK_MODE = "feedback_mode";
        public static final String QUESTION_ORDER = "question_order";

        public static final String TOTAL_QUESTIONS = "total_questions";
        public static final String CORRECT_COUNT = "correct_count";
        public static final String INCORRECT_COUNT = "incorrect_count";

        public static final String STATUS = "status";

        public static final String STARTED_AT = "started_at";
        public static final String FINISHED_AT = "finished_at";
    }

    // =====================================================
    // 12. SESSION ANSWERS — ОТВЕТЫ В УЧЕБНЫХ СЕССИЯХ
    // =====================================================

    public static final class SessionAnswers {

        public static final String TABLE = "session_answers";

        public static final String ID = "id";

        public static final String SESSION_ID = "session_id";
        public static final String QUESTION_ID = "question_id";

        public static final String SELECTED_ANSWER = "selected_answer";
        public static final String IS_CORRECT = "is_correct";

        public static final String ANSWERED_AT = "answered_at";
        public static final String ATTEMPT_NUMBER = "attempt_number";
    }

    // =====================================================
    // 13. SESSION QUESTIONS — ВОПРОСЫ УЧЕБНОЙ СЕССИИ
    // =====================================================

    public static final class SessionQuestions {

        public static final String TABLE = "session_questions";

        public static final String SESSION_ID = "session_id";
        public static final String QUESTION_ID = "question_id";
        public static final String SORT_ORDER = "sort_order";
    }

    // =====================================================
    // 14. IMPORT HISTORY — ИСТОРИЯ ИМПОРТА
    // =====================================================

    public static final class ImportHistory {

        public static final String TABLE = "import_history";

        public static final String ID = "id";

        public static final String PACK_UID = "pack_uid";
        public static final String PACK_VERSION = "pack_version";

        public static final String FILE_NAME = "file_name";
        public static final String CHECKSUM = "checksum";

        public static final String STATUS = "status";
        public static final String IMPORTED_COUNT = "imported_count";
        public static final String ERROR_COUNT = "error_count";

        public static final String STARTED_AT = "started_at";
        public static final String FINISHED_AT = "finished_at";
    }

    // =====================================================
    // 15. IMPORT ERRORS — ОШИБКИ ИМПОРТА
    // =====================================================

    public static final class ImportErrors {

        public static final String TABLE = "import_errors";

        public static final String ID = "id";
        public static final String IMPORT_ID = "import_id";

        public static final String ENTITY_TYPE = "entity_type";
        public static final String EXTERNAL_ID = "external_id";

        public static final String SOURCE_PAGE = "source_page";
        public static final String SEVERITY = "severity";
        public static final String MESSAGE = "message";

        public static final String CREATED_AT = "created_at";
    }
}

