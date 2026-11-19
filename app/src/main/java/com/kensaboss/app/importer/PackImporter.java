
package com.kensaboss.app.importer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kensaboss.app.data.database.DatabaseContract;
import com.kensaboss.app.data.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public final class PackImporter {

    private PackImporter() {
    }

    /**
     * Импортирует проверенный пакет в локальную SQLite.
     *
     * Повторный импорт пакета с тем же packUid запрещён.
     * Все изменения базы выполняются в одной транзакции.
     * Файлы извлекаются в приватную папку приложения.
     */
    public static PackImportResult importPack(
            Context context,
            File packageFile,
            PackImportData data
    ) {

        PackImportResult result = new PackImportResult();

        if (context == null) {
            result.addError("Context must not be null");
            return result;
        }

        Context appContext = context.getApplicationContext();

        if (packageFile == null
                || !packageFile.exists()
                || !packageFile.isFile()
                || !packageFile.canRead()) {

            result.addError("Package file is missing or unreadable");
            return result;
        }

        if (data == null) {
            result.addError("Import data must not be null");
            return result;
        }

        PackManifest manifest = data.getManifest();

        if (manifest == null) {
            result.addError("Package manifest is missing");
            return result;
        }

        // Проверяем ZIP и сверяем его манифест с переданными данными.
        try {

            PackManifest archiveManifest =
                    KbPackReader.validatePackage(packageFile);

            if (archiveManifest == null
                    || !manifest.getPackUid().equals(
                    archiveManifest.getPackUid()
            )) {

                result.addError(
                        "Package manifest does not match import data"
                );

                return result;
            }

        } catch (Exception exception) {

            result.addError(
                    "Package validation failed: "
                            + safeMessage(exception)
            );

            return result;
        }

        PackImportResult validation =
                PackImportValidator.validate(data);

        if (!validation.isSuccessful()) {

            for (String error : validation.getErrors()) {
                result.addError(error);
            }

            result.setStatus(PackImportResult.STATUS_FAILED);
            return result;
        }

        String checksum;

        try {
            checksum = calculateChecksum(packageFile);
        } catch (Exception exception) {
            result.addError(
                    "Cannot calculate package checksum: "
                            + safeMessage(exception)
            );
            return result;
        }

        DatabaseHelper helper = new DatabaseHelper(appContext);

        SQLiteDatabase db = null;

        boolean transactionStarted = false;
        boolean importCommitted = false;

        Map<String, String> extractedAssetPaths = new HashMap<>();

        File extractedAssetDirectory = null;

        try {

            db = helper.getWritableDatabase();

            db.beginTransaction();
            transactionStarted = true;

            String packUid = manifest.getPackUid();

            if (packExists(db, packUid)) {
                throw new IllegalStateException(
                        "This content pack is already imported: "
                                + packUid
                );
            }

            // Извлекаем файлы после проверки дубликата пакета.
            extractedAssetPaths =
                    PackAssetExtractor.extractAssets(
                            appContext,
                            packageFile,
                            packUid,
                            data.getExplanationAssets()
                    );

            extractedAssetDirectory = getAssetDirectory(
                    appContext,
                    extractedAssetPaths
            );

            String startedAt = currentTimestamp();

            long importId = insertImportHistory(
                    db,
                    manifest,
                    packageFile,
                    checksum,
                    startedAt
            );

            long packId = insertContentPack(
                    db,
                    manifest,
                    checksum
            );

            Map<String, Long> bookIds = new HashMap<>();
            Map<String, Long> partIds = new HashMap<>();
            Map<String, Long> chapterIds = new HashMap<>();
            Map<String, Long> sectionIds = new HashMap<>();
            Map<String, Long> questionIds = new HashMap<>();

            Map<String, Long> optionIds = new HashMap<>();
            Map<String, Long> explanationIds = new HashMap<>();

            int booksCount = importBooks(
                    db,
                    data.getBooks(),
                    packId,
                    manifest,
                    bookIds
            );

            int partsCount = importBookParts(
                    db,
                    data.getBookParts(),
                    bookIds,
                    partIds
            );

            int chaptersCount = importChapters(
                    db,
                    data.getChapters(),
                    partIds,
                    chapterIds
            );

            int sectionsCount = importSections(
                    db,
                    data.getSections(),
                    chapterIds,
                    sectionIds
            );

            int questionsCount = importQuestions(
                    db,
                    data.getQuestions(),
                    bookIds,
                    chapterIds,
                    sectionIds,
                    questionIds
            );

            int optionsCount = importAnswerOptions(
                    db,
                    data.getAnswerOptions(),
                    questionIds,
                    optionIds
            );

            int answersCount = importCorrectAnswers(
                    db,
                    data.getCorrectAnswers(),
                    questionIds,
                    optionIds
            );

            int explanationsCount = importExplanations(
                    db,
                    data.getExplanations(),
                    questionIds,
                    explanationIds
            );

            int assetsCount = importExplanationAssets(
                    db,
                    data.getExplanationAssets(),
                    questionIds,
                    explanationIds,
                    extractedAssetPaths
            );

            result.setImportedBooks(booksCount);
            result.setImportedBookParts(partsCount);
            result.setImportedChapters(chaptersCount);
            result.setImportedSections(sectionsCount);
            result.setImportedQuestions(questionsCount);

            result.setImportedAnswerOptions(optionsCount);
            result.setImportedCorrectAnswers(answersCount);
            result.setImportedExplanations(explanationsCount);
            result.setImportedExplanationAssets(assetsCount);

            updateImportHistory(
                    db,
                    importId,
                    PackImportResult.STATUS_SUCCESS,
                    result.getTotalImported(),
                    0,
                    currentTimestamp()
            );

            db.setTransactionSuccessful();

            importCommitted = true;

            result.setStatus(
                    PackImportResult.STATUS_SUCCESS
            );

        } catch (Exception exception) {

            result.addError(
                    exception.getClass().getSimpleName()
                            + ": "
                            + safeMessage(exception)
            );

            result.setStatus(
                    PackImportResult.STATUS_FAILED
            );

        } finally {

            if (db != null && transactionStarted) {
                try {
                    db.endTransaction();
                } catch (Exception exception) {
                    result.addError(
                            "Transaction closing failed: "
                                    + safeMessage(exception)
                    );

                    importCommitted = false;

                    result.setStatus(
                            PackImportResult.STATUS_FAILED
                    );
                }
            }

            helper.close();

            // Если импорт не завершился успешно,
            // удаляем извлечённые файлы.
            if (!importCommitted) {
                PackAssetExtractor.deleteRecursively(
                        extractedAssetDirectory
                );
            }
        }

        return result;
    }

    /**
     * Определяет папку пакета по сохранённому относительному пути.
     */
    private static File getAssetDirectory(
            Context context,
            Map<String, String> extractedPaths
    ) {

        if (extractedPaths == null || extractedPaths.isEmpty()) {
            return null;
        }

        String relativePath =
                extractedPaths.values().iterator().next();

        String prefix = "content_packs/";

        if (!relativePath.startsWith(prefix)) {
            throw new IllegalStateException(
                    "Unexpected extracted asset path"
            );
        }

        int slash = relativePath.indexOf(
                '/',
                prefix.length()
        );

        if (slash <= prefix.length()) {
            throw new IllegalStateException(
                    "Invalid extracted asset directory"
            );
        }

        String directoryName = relativePath.substring(
                prefix.length(),
                slash
        );

        return new File(
                new File(context.getFilesDir(), "content_packs"),
                directoryName
        );
    }

    // =====================================================
    // CONTENT PACK
    // =====================================================

    private static long insertContentPack(
            SQLiteDatabase db,
            PackManifest manifest,
            String checksum
    ) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ContentPacks.PACK_UID,
                manifest.getPackUid()
        );

        values.put(
                DatabaseContract.ContentPacks.TITLE,
                manifest.getTitle()
        );

        values.put(
                DatabaseContract.ContentPacks.LANGUAGE,
                manifest.getLanguage()
        );

        values.put(
                DatabaseContract.ContentPacks.VERSION,
                manifest.getVersion()
        );

        values.put(
                DatabaseContract.ContentPacks.DESCRIPTION,
                manifest.getDescription()
        );

        values.put(
                DatabaseContract.ContentPacks.CHECKSUM,
                checksum
        );

        String createdAt = manifest.getCreatedAt();

        values.put(
                DatabaseContract.ContentPacks.CREATED_AT,
                isEmpty(createdAt)
                        ? currentTimestamp()
                        : createdAt
        );

        return insertOrThrow(
                db,
                DatabaseContract.ContentPacks.TABLE,
                values
        );
    }

    // =====================================================
    // BOOKS
    // =====================================================

    private static int importBooks(
            SQLiteDatabase db,
            JSONArray array,
            long packId,
            PackManifest manifest,
            Map<String, Long> ids
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String externalId = requiredString(
                    item,
                    "externalId"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.Books.PACK_ID,
                    packId
            );

            values.put(
                    DatabaseContract.Books.EXTERNAL_ID,
                    externalId
            );

            values.put(
                    DatabaseContract.Books.TITLE,
                    requiredText(item, "title")
            );

            values.put(
                    DatabaseContract.Books.AUTHOR,
                    optionalString(item, "author")
            );

            String language = optionalString(
                    item,
                    "language"
            );

            values.put(
                    DatabaseContract.Books.LANGUAGE,
                    isEmpty(language)
                            ? manifest.getLanguage()
                            : language
            );

            values.put(
                    DatabaseContract.Books.DESCRIPTION,
                    optionalString(item, "description")
            );

            values.put(
                    DatabaseContract.Books.VERSION,
                    optionalStringOrDefault(
                            item,
                            "version",
                            "1"
                    )
            );

            values.put(
                    DatabaseContract.Books.CREATED_AT,
                    currentTimestamp()
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.Books.TABLE,
                    values
            );

            ids.put(externalId, id);
            count++;
        }

        return count;
    }

    // =====================================================
    // BOOK PARTS
    // =====================================================

    private static int importBookParts(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> bookIds,
            Map<String, Long> ids
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String externalId = requiredString(
                    item,
                    "externalId"
            );

            long bookId = requiredMappedId(
                    bookIds,
                    requiredString(item, "bookExternalId"),
                    "book"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.BookParts.BOOK_ID,
                    bookId
            );

            values.put(
                    DatabaseContract.BookParts.EXTERNAL_ID,
                    externalId
            );

            values.put(
                    DatabaseContract.BookParts.TITLE,
                    requiredText(item, "title")
            );

            values.put(
                    DatabaseContract.BookParts.PART_NUMBER,
                    item.getInt("partNumber")
            );

            values.put(
                    DatabaseContract.BookParts.SORT_ORDER,
                    item.optInt("sortOrder", 0)
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.BookParts.TABLE,
                    values
            );

            ids.put(externalId, id);
            count++;
        }

        return count;
    }

    // =====================================================
    // CHAPTERS
    // =====================================================

    private static int importChapters(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> partIds,
            Map<String, Long> ids
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String externalId = requiredString(
                    item,
                    "externalId"
            );

            long partId = requiredMappedId(
                    partIds,
                    requiredString(item, "bookPartExternalId"),
                    "book part"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.Chapters.BOOK_PART_ID,
                    partId
            );

            values.put(
                    DatabaseContract.Chapters.EXTERNAL_ID,
                    externalId
            );

            values.put(
                    DatabaseContract.Chapters.TITLE,
                    requiredText(item, "title")
            );

            values.put(
                    DatabaseContract.Chapters.CHAPTER_NUMBER,
                    item.getInt("chapterNumber")
            );

            values.put(
                    DatabaseContract.Chapters.SORT_ORDER,
                    item.optInt("sortOrder", 0)
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.Chapters.TABLE,
                    values
            );

            ids.put(externalId, id);
            count++;
        }

        return count;
    }

    // =====================================================
    // SECTIONS
    // =====================================================

    private static int importSections(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> chapterIds,
            Map<String, Long> ids
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String externalId = requiredString(
                    item,
                    "externalId"
            );

            long chapterId = requiredMappedId(
                    chapterIds,
                    requiredString(item, "chapterExternalId"),
                    "chapter"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.Sections.CHAPTER_ID,
                    chapterId
            );

            values.put(
                    DatabaseContract.Sections.EXTERNAL_ID,
                    externalId
            );

            values.put(
                    DatabaseContract.Sections.TITLE,
                    requiredText(item, "title")
            );

            values.put(
                    DatabaseContract.Sections.SORT_ORDER,
                    item.optInt("sortOrder", 0)
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.Sections.TABLE,
                    values
            );

            ids.put(externalId, id);
            count++;
        }

        return count;
    }

    // =====================================================
    // QUESTIONS
    // =====================================================

    private static int importQuestions(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> bookIds,
            Map<String, Long> chapterIds,
            Map<String, Long> sectionIds,
            Map<String, Long> ids
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String externalId = requiredString(
                    item,
                    "externalId"
            );

            long bookId = requiredMappedId(
                    bookIds,
                    requiredString(item, "bookExternalId"),
                    "book"
            );

            long chapterId = requiredMappedId(
                    chapterIds,
                    requiredString(item, "chapterExternalId"),
                    "chapter"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.Questions.BOOK_ID,
                    bookId
            );

            values.put(
                    DatabaseContract.Questions.CHAPTER_ID,
                    chapterId
            );

            String sectionExternalId = optionalString(
                    item,
                    "sectionExternalId"
            );

            if (!isEmpty(sectionExternalId)) {

                long sectionId = requiredMappedId(
                        sectionIds,
                        sectionExternalId,
                        "section"
                );

                values.put(
                        DatabaseContract.Questions.SECTION_ID,
                        sectionId
                );
            }

            values.put(
                    DatabaseContract.Questions.EXTERNAL_ID,
                    externalId
            );

            values.put(
                    DatabaseContract.Questions.QUESTION_TEXT,
                    requiredText(item, "questionText")
            );

            values.put(
                    DatabaseContract.Questions.QUESTION_TYPE,
                    requiredString(item, "questionType")
            );

            putOptionalString(
                    values,
                    DatabaseContract.Questions.ORIGINAL_NUMBER,
                    item,
                    "originalNumber"
            );

            putOptionalInteger(
                    values,
                    DatabaseContract.Questions.SOURCE_PAGE,
                    item,
                    "sourcePage"
            );

            values.put(
                    DatabaseContract.Questions.DIFFICULTY,
                    item.optInt("difficulty", 0)
            );

            values.put(
                    DatabaseContract.Questions.SORT_ORDER,
                    item.optInt("sortOrder", 0)
            );

            values.put(
                    DatabaseContract.Questions.SOURCE_KEY_STATUS,
                    optionalStringOrDefault(
                            item,
                            "sourceKeyStatus",
                            "NOT_CHECKED"
                    )
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.Questions.TABLE,
                    values
            );

            ids.put(externalId, id);
            count++;
        }

        return count;
    }

    // =====================================================
    // ANSWER OPTIONS
    // =====================================================

    private static int importAnswerOptions(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> questionIds,
            Map<String, Long> optionIds
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String externalId = requiredString(
                    item,
                    "externalId"
            );

            String questionExternalId = requiredString(
                    item,
                    "questionExternalId"
            );

            long questionId = requiredMappedId(
                    questionIds,
                    questionExternalId,
                    "question"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.AnswerOptions.QUESTION_ID,
                    questionId
            );

            values.put(
                    DatabaseContract.AnswerOptions.EXTERNAL_ID,
                    externalId
            );

            values.put(
                    DatabaseContract.AnswerOptions.OPTION_TEXT,
                    requiredText(item, "optionText")
            );

            values.put(
                    DatabaseContract.AnswerOptions.OPTION_LABEL,
                    optionalString(item, "optionLabel")
            );

            values.put(
                    DatabaseContract.AnswerOptions.SORT_ORDER,
                    item.getInt("sortOrder")
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.AnswerOptions.TABLE,
                    values
            );

            optionIds.put(
                    optionMapKey(questionExternalId, externalId),
                    id
            );

            count++;
        }

        return count;
    }

    // =====================================================
    // CORRECT ANSWERS
    // =====================================================

    private static int importCorrectAnswers(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> questionIds,
            Map<String, Long> optionIds
    ) throws JSONException {

        int count = 0;

        Map<String, Integer> answerOrders = new HashMap<>();

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String questionExternalId = requiredString(
                    item,
                    "questionExternalId"
            );

            long questionId = requiredMappedId(
                    questionIds,
                    questionExternalId,
                    "question"
            );

            String optionExternalId = optionalString(
                    item,
                    "optionExternalId"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.CorrectAnswers.QUESTION_ID,
                    questionId
            );

            String answerType;

            if (!isEmpty(optionExternalId)) {

                long optionId = requiredMappedId(
                        optionIds,
                        optionMapKey(
                                questionExternalId,
                                optionExternalId
                        ),
                        "answer option"
                );

                values.put(
                        DatabaseContract.CorrectAnswers.OPTION_ID,
                        optionId
                );

                values.putNull(
                        DatabaseContract.CorrectAnswers.ANSWER_VALUE
                );

                answerType = "OPTION";

            } else {

                Object rawValue = item.opt("answerValue");

                if (rawValue == null
                        || rawValue == JSONObject.NULL) {

                    throw new IllegalArgumentException(
                            "Correct answer has no answerValue"
                    );
                }

                String answerValue = String.valueOf(rawValue);

                values.putNull(
                        DatabaseContract.CorrectAnswers.OPTION_ID
                );

                values.put(
                        DatabaseContract.CorrectAnswers.ANSWER_VALUE,
                        answerValue
                );

                answerType = rawValue instanceof Number
                        ? "NUMERIC"
                        : "TEXT";
            }

            int order = answerOrders.containsKey(questionExternalId)
                    ? answerOrders.get(questionExternalId)
                    : 0;

            values.put(
                    DatabaseContract.CorrectAnswers.ANSWER_TYPE,
                    answerType
            );

            values.put(
                    DatabaseContract.CorrectAnswers.SORT_ORDER,
                    order
            );

            answerOrders.put(
                    questionExternalId,
                    order + 1
            );

            Object sourceValue = item.opt("sourceValue");

            if (sourceValue == null || sourceValue == JSONObject.NULL) {
                values.putNull(
                        DatabaseContract.CorrectAnswers.SOURCE_VALUE
                );
            } else {
                values.put(
                        DatabaseContract.CorrectAnswers.SOURCE_VALUE,
                        String.valueOf(sourceValue)
                );
            }

            values.put(
                    DatabaseContract.CorrectAnswers.VERIFICATION_STATUS,
                    optionalStringOrDefault(
                            item,
                            "verificationStatus",
                            "NOT_VERIFIED"
                    ).toUpperCase(Locale.ROOT)
            );

            values.put(
                    DatabaseContract.CorrectAnswers.VERIFICATION_NOTES,
                    optionalString(item, "verificationNotes")
            );

            values.put(
                    DatabaseContract.CorrectAnswers.VERIFIED_AT,
                    optionalString(item, "verifiedAt")
            );

            insertOrThrow(
                    db,
                    DatabaseContract.CorrectAnswers.TABLE,
                    values
            );

            count++;
        }

        return count;
    }

    // =====================================================
    // EXPLANATIONS
    // =====================================================

    private static int importExplanations(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> questionIds,
            Map<String, Long> explanationIds
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String questionExternalId = requiredString(
                    item,
                    "questionExternalId"
            );

            long questionId = requiredMappedId(
                    questionIds,
                    questionExternalId,
                    "question"
            );

            String language = requiredString(
                    item,
                    "language"
            );

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.Explanations.QUESTION_ID,
                    questionId
            );

            values.put(
                    DatabaseContract.Explanations.LANGUAGE,
                    language
            );

            // Текст объяснения сохраняется без обрезания пробелов.
            values.put(
                    DatabaseContract.Explanations.INTRODUCTION,
                    optionalString(item, "introduction")
            );

            values.put(
                    DatabaseContract.Explanations.DETAILED_TEXT,
                    optionalString(item, "detailedText")
            );

            values.put(
                    DatabaseContract.Explanations.SOLUTION_STEPS,
                    optionalString(item, "solutionSteps")
            );

            values.put(
                    DatabaseContract.Explanations.ADDITIONAL_NOTES,
                    optionalString(item, "additionalNotes")
            );

            values.put(
                    DatabaseContract.Explanations.VERSION,
                    item.optInt("version", 1)
            );

            long id = insertOrThrow(
                    db,
                    DatabaseContract.Explanations.TABLE,
                    values
            );

            explanationIds.put(
                    explanationMapKey(questionExternalId, language),
                    id
            );

            count++;
        }

        return count;
    }

    // =====================================================
    // EXPLANATION ASSETS
    // =====================================================

    private static int importExplanationAssets(
            SQLiteDatabase db,
            JSONArray array,
            Map<String, Long> questionIds,
            Map<String, Long> explanationIds,
            Map<String, String> extractedAssetPaths
    ) throws JSONException {

        int count = 0;

        for (int i = 0; i < array.length(); i++) {

            JSONObject item = array.getJSONObject(i);

            String questionExternalId = requiredString(
                    item,
                    "questionExternalId"
            );

            long questionId = requiredMappedId(
                    questionIds,
                    questionExternalId,
                    "question"
            );

            String language = requiredString(
                    item,
                    "language"
            );

            long explanationId = requiredMappedId(
                    explanationIds,
                    explanationMapKey(
                            questionExternalId,
                            language
                    ),
                    "explanation"
            );

            String originalFilePath = requiredString(
                    item,
                    "filePath"
            );

            String storedFilePath =
                    extractedAssetPaths.get(originalFilePath);

            if (isEmpty(storedFilePath)) {
                throw new IllegalStateException(
                        "Extracted asset path not found: "
                                + originalFilePath
                );
            }

            ContentValues values = new ContentValues();

            values.put(
                    DatabaseContract.ExplanationAssets.EXPLANATION_ID,
                    explanationId
            );

            values.put(
                    DatabaseContract.ExplanationAssets.ASSET_TYPE,
                    requiredString(item, "assetType")
            );

            // Сохраняем путь внутри приватной папки приложения,
            // а не исходный путь из ZIP.
            values.put(
                    DatabaseContract.ExplanationAssets.FILE_PATH,
                    storedFilePath
            );

            values.put(
                    DatabaseContract.ExplanationAssets.DESCRIPTION,
                    optionalString(item, "description")
            );

            values.put(
                    DatabaseContract.ExplanationAssets.SORT_ORDER,
                    item.optInt("sortOrder", 0)
            );

            insertOrThrow(
                    db,
                    DatabaseContract.ExplanationAssets.TABLE,
                    values
            );

            count++;
        }

        return count;
    }

    // =====================================================
    // IMPORT HISTORY
    // =====================================================

    private static long insertImportHistory(
            SQLiteDatabase db,
            PackManifest manifest,
            File packageFile,
            String checksum,
            String startedAt
    ) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ImportHistory.PACK_UID,
                manifest.getPackUid()
        );

        values.put(
                DatabaseContract.ImportHistory.PACK_VERSION,
                manifest.getVersion()
        );

        values.put(
                DatabaseContract.ImportHistory.FILE_NAME,
                packageFile.getName()
        );

        values.put(
                DatabaseContract.ImportHistory.CHECKSUM,
                checksum
        );

        values.put(
                DatabaseContract.ImportHistory.STATUS,
                "SUCCESS"
        );

        values.put(
                DatabaseContract.ImportHistory.IMPORTED_COUNT,
                0
        );

        values.put(
                DatabaseContract.ImportHistory.ERROR_COUNT,
                0
        );

        values.put(
                DatabaseContract.ImportHistory.STARTED_AT,
                startedAt
        );

        values.putNull(
                DatabaseContract.ImportHistory.FINISHED_AT
        );

        return insertOrThrow(
                db,
                DatabaseContract.ImportHistory.TABLE,
                values
        );
    }

    private static void updateImportHistory(
            SQLiteDatabase db,
            long importId,
            String status,
            int importedCount,
            int errorCount,
            String finishedAt
    ) {

        ContentValues values = new ContentValues();

        values.put(
                DatabaseContract.ImportHistory.STATUS,
                status
        );

        values.put(
                DatabaseContract.ImportHistory.IMPORTED_COUNT,
                importedCount
        );

        values.put(
                DatabaseContract.ImportHistory.ERROR_COUNT,
                errorCount
        );

        values.put(
                DatabaseContract.ImportHistory.FINISHED_AT,
                finishedAt
        );

        int updated = db.update(
                DatabaseContract.ImportHistory.TABLE,
                values,
                DatabaseContract.ImportHistory.ID + " = ?",
                new String[]{String.valueOf(importId)}
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Cannot update import history"
            );
        }
    }

    // =====================================================
    // DATABASE HELPERS
    // =====================================================

    private static long insertOrThrow(
            SQLiteDatabase db,
            String table,
            ContentValues values
    ) {

        long id = db.insertOrThrow(
                table,
                null,
                values
        );

        if (id == -1) {
            throw new IllegalStateException(
                    "Cannot insert into table: " + table
            );
        }

        return id;
    }

    private static boolean packExists(
            SQLiteDatabase db,
            String packUid
    ) {

        Cursor cursor = db.query(
                DatabaseContract.ContentPacks.TABLE,
                new String[]{
                        DatabaseContract.ContentPacks.ID
                },
                DatabaseContract.ContentPacks.PACK_UID + " = ?",
                new String[]{packUid},
                null,
                null,
                null
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private static long requiredMappedId(
            Map<String, Long> map,
            String externalId,
            String entityName
    ) {

        Long id = map.get(externalId);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Unknown " + entityName
                            + " externalId: " + externalId
            );
        }

        return id;
    }

    // =====================================================
    // JSON HELPERS
    // =====================================================

    /**
     * Читает служебную строку и удаляет внешние пробелы.
     * Используется для ID, типов, языков и других ключей.
     */
    private static String requiredString(
            JSONObject object,
            String key
    ) throws JSONException {

        Object value = object.opt(key);

        if (value == null
                || value == JSONObject.NULL
                || !(value instanceof String)
                || ((String) value).trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Required string is missing: " + key
            );
        }

        return ((String) value).trim();
    }

    /**
     * Читает исходный текст, проверяя его содержимое,
     * но не изменяет пробелы и переносы строк.
     */
    private static String requiredText(
            JSONObject object,
            String key
    ) throws JSONException {

        Object value = object.opt(key);

        if (value == null
                || value == JSONObject.NULL
                || !(value instanceof String)
                || ((String) value).trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Required text is missing: " + key
            );
        }

        return (String) value;
    }

    private static String optionalString(
            JSONObject object,
            String key
    ) {

        Object value = object.opt(key);

        if (value == null || value == JSONObject.NULL) {
            return null;
        }

        return String.valueOf(value);
    }

    private static String optionalStringOrDefault(
            JSONObject object,
            String key,
            String defaultValue
    ) {

        String value = optionalString(object, key);

        return isEmpty(value)
                ? defaultValue
                : value.trim();
    }

    private static void putOptionalString(
            ContentValues values,
            String column,
            JSONObject object,
            String key
    ) {

        Object value = object.opt(key);

        if (value == null || value == JSONObject.NULL) {
            values.putNull(column);
        } else {
            values.put(column, String.valueOf(value));
        }
    }

    private static void putOptionalInteger(
            ContentValues values,
            String column,
            JSONObject object,
            String key
    ) throws JSONException {

        Object value = object.opt(key);

        if (value == null || value == JSONObject.NULL) {
            values.putNull(column);
        } else if (value instanceof Number) {
            values.put(column, ((Number) value).intValue());
        } else {
            throw new IllegalArgumentException(
                    "Expected integer: " + key
            );
        }
    }

    // =====================================================
    // GENERAL HELPERS
    // =====================================================

    private static String optionMapKey(
            String questionExternalId,
            String optionExternalId
    ) {
        return questionExternalId + "::" + optionExternalId;
    }

    private static String explanationMapKey(
            String questionExternalId,
            String language
    ) {
        return questionExternalId + "::" + language;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String currentTimestamp() {

        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.US
        );

        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return format.format(new Date());
    }

    private static String safeMessage(Exception exception) {

        String message = exception.getMessage();

        return message == null
                ? "No additional details"
                : message;
    }

    private static String calculateChecksum(File file)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        try (FileInputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];

            int read;

            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        byte[] hash = digest.digest();

        StringBuilder result = new StringBuilder();

        for (byte value : hash) {
            result.append(
                    String.format(
                            Locale.ROOT,
                            "%02x",
                            value & 0xff
                    )
            );
        }

        return result.toString();
    }
}
