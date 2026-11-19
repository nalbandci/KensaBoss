
package com.kensaboss.app.importer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PackDataValidator {

    // =====================================================
    // 1. BOOKS
    // =====================================================

    public static List<String> validateBooks(JSONArray books) {

        List<String> errors = new ArrayList<>();

        if (books == null || books.length() == 0) {
            errors.add("Books array is empty.");
            return errors;
        }

        Set<String> externalIds = new HashSet<>();

        for (int i = 0; i < books.length(); i++) {

            JSONObject book = books.optJSONObject(i);

            if (book == null) {
                errors.add("Book at index " + i + " is not an object.");
                continue;
            }

            String externalId = getString(book, "externalId");
            String title = getString(book, "title");
            String language = getString(book, "language");
            String version = getString(book, "version");

            if (externalId == null) {
                errors.add("Book at index " + i + ": externalId is required.");
            } else if (!externalIds.add(externalId)) {
                errors.add("Duplicate book externalId: " + externalId);
            }

            if (title == null) {
                errors.add("Book at index " + i + ": title is required.");
            }

            if (language == null) {
                errors.add("Book at index " + i + ": language is required.");
            }

            if (version == null) {
                errors.add("Book at index " + i + ": version is required.");
            }

            validateOptionalString(book, "author", i, errors, "Book");
            validateOptionalString(book, "description", i, errors, "Book");
        }

        return errors;
    }

    // =====================================================
    // 2. BOOK PARTS
    // =====================================================

    public static List<String> validateBookParts(
            JSONArray bookParts,
            JSONArray books
    ) {

        List<String> errors = new ArrayList<>();

        if (bookParts == null || bookParts.length() == 0) {
            errors.add("Book parts array is empty.");
            return errors;
        }

        Set<String> knownBookIds = new HashSet<>();

        if (books != null) {
            for (int i = 0; i < books.length(); i++) {

                JSONObject book = books.optJSONObject(i);

                if (book != null) {
                    String id = getString(book, "externalId");

                    if (id != null) {
                        knownBookIds.add(id);
                    }
                }
            }
        }

        Set<String> partIds = new HashSet<>();
        Set<String> partNumbers = new HashSet<>();

        for (int i = 0; i < bookParts.length(); i++) {

            JSONObject part = bookParts.optJSONObject(i);

            if (part == null) {
                errors.add("Book part at index " + i + " is not an object.");
                continue;
            }

            String externalId = getString(part, "externalId");
            String bookExternalId = getString(part, "bookExternalId");
            String title = getString(part, "title");

            if (externalId == null) {
                errors.add("Book part at index " + i + ": externalId is required.");
            } else if (!partIds.add(externalId)) {
                errors.add("Duplicate book part externalId: " + externalId);
            }

            if (bookExternalId == null) {
                errors.add("Book part at index " + i + ": bookExternalId is required.");
            } else if (!knownBookIds.contains(bookExternalId)) {
                errors.add(
                        "Book part at index " + i
                                + " references an unknown book: "
                                + bookExternalId
                );
            }

            if (title == null) {
                errors.add("Book part at index " + i + ": title is required.");
            }

            Object numberValue = part.opt("partNumber");

            if (!(numberValue instanceof Integer)
                    && !(numberValue instanceof Long)) {

                errors.add(
                        "Book part at index " + i
                                + ": partNumber must be an integer."
                );

            } else {

                long partNumber = ((Number) numberValue).longValue();

                if (partNumber <= 0) {
                    errors.add(
                            "Book part at index " + i
                                    + ": partNumber must be greater than zero."
                    );
                } else if (bookExternalId != null) {

                    String uniqueNumber = bookExternalId + ":" + partNumber;

                    if (!partNumbers.add(uniqueNumber)) {
                        errors.add(
                                "Duplicate partNumber " + partNumber
                                        + " in book " + bookExternalId
                        );
                    }
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 3. CHAPTERS
    // =====================================================

    public static List<String> validateChapters(
            JSONArray chapters,
            JSONArray bookParts
    ) {

        List<String> errors = new ArrayList<>();

        if (chapters == null || chapters.length() == 0) {
            errors.add("Chapters array is empty.");
            return errors;
        }

        Set<String> knownPartIds = new HashSet<>();

        if (bookParts != null) {

            for (int i = 0; i < bookParts.length(); i++) {

                JSONObject part = bookParts.optJSONObject(i);

                if (part != null) {

                    String id = getString(part, "externalId");

                    if (id != null) {
                        knownPartIds.add(id);
                    }
                }
            }
        }

        Set<String> chapterIds = new HashSet<>();
        Set<String> chapterNumbers = new HashSet<>();

        for (int i = 0; i < chapters.length(); i++) {

            JSONObject chapter = chapters.optJSONObject(i);

            if (chapter == null) {
                errors.add("Chapter at index " + i + " is not an object.");
                continue;
            }

            String externalId = getString(chapter, "externalId");
            String bookPartExternalId = getString(chapter, "bookPartExternalId");
            String title = getString(chapter, "title");

            if (externalId == null) {
                errors.add(
                        "Chapter at index " + i
                                + ": externalId is required."
                );
            } else if (!chapterIds.add(externalId)) {
                errors.add("Duplicate chapter externalId: " + externalId);
            }

            if (bookPartExternalId == null) {
                errors.add(
                        "Chapter at index " + i
                                + ": bookPartExternalId is required."
                );
            } else if (!knownPartIds.contains(bookPartExternalId)) {
                errors.add(
                        "Chapter at index " + i
                                + " references an unknown book part: "
                                + bookPartExternalId
                );
            }

            if (title == null) {
                errors.add(
                        "Chapter at index " + i
                                + ": title is required."
                );
            }

            Object numberValue = chapter.opt("chapterNumber");

            if (!(numberValue instanceof Integer)
                    && !(numberValue instanceof Long)) {

                errors.add(
                        "Chapter at index " + i
                                + ": chapterNumber must be an integer."
                );

            } else {

                long chapterNumber = ((Number) numberValue).longValue();

                if (chapterNumber <= 0) {
                    errors.add(
                            "Chapter at index " + i
                                    + ": chapterNumber must be greater than zero."
                    );
                } else if (bookPartExternalId != null) {

                    String uniqueNumber =
                            bookPartExternalId + ":" + chapterNumber;

                    if (!chapterNumbers.add(uniqueNumber)) {
                        errors.add(
                                "Duplicate chapterNumber "
                                        + chapterNumber
                                        + " in book part "
                                        + bookPartExternalId
                        );
                    }
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 4. SECTIONS
    // =====================================================

    public static List<String> validateSections(
            JSONArray sections,
            JSONArray chapters
    ) {

        List<String> errors = new ArrayList<>();

        if (sections == null || sections.length() == 0) {
            errors.add("Sections array is empty.");
            return errors;
        }

        Set<String> knownChapterIds = new HashSet<>();

        if (chapters != null) {

            for (int i = 0; i < chapters.length(); i++) {

                JSONObject chapter = chapters.optJSONObject(i);

                if (chapter != null) {

                    String id = getString(chapter, "externalId");

                    if (id != null) {
                        knownChapterIds.add(id);
                    }
                }
            }
        }

        Set<String> sectionIds = new HashSet<>();
        Set<String> sectionOrders = new HashSet<>();

        for (int i = 0; i < sections.length(); i++) {

            JSONObject section = sections.optJSONObject(i);

            if (section == null) {
                errors.add("Section at index " + i + " is not an object.");
                continue;
            }

            String externalId = getString(section, "externalId");
            String chapterExternalId = getString(section, "chapterExternalId");
            String title = getString(section, "title");

            if (externalId == null) {
                errors.add(
                        "Section at index " + i
                                + ": externalId is required."
                );
            } else if (!sectionIds.add(externalId)) {
                errors.add("Duplicate section externalId: " + externalId);
            }

            if (chapterExternalId == null) {
                errors.add(
                        "Section at index " + i
                                + ": chapterExternalId is required."
                );
            } else if (!knownChapterIds.contains(chapterExternalId)) {
                errors.add(
                        "Section at index " + i
                                + " references an unknown chapter: "
                                + chapterExternalId
                );
            }

            if (title == null) {
                errors.add(
                        "Section at index " + i
                                + ": title is required."
                );
            }

            Object orderValue = section.opt("sortOrder");

            if (!(orderValue instanceof Integer)
                    && !(orderValue instanceof Long)) {

                errors.add(
                        "Section at index " + i
                                + ": sortOrder must be an integer."
                );

            } else {

                long sortOrder = ((Number) orderValue).longValue();

                if (sortOrder <= 0) {
                    errors.add(
                            "Section at index " + i
                                    + ": sortOrder must be greater than zero."
                    );
                } else if (chapterExternalId != null) {

                    String uniqueOrder =
                            chapterExternalId + ":" + sortOrder;

                    if (!sectionOrders.add(uniqueOrder)) {
                        errors.add(
                                "Duplicate sortOrder " + sortOrder
                                        + " in chapter "
                                        + chapterExternalId
                        );
                    }
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 5. QUESTIONS
    // =====================================================

    /*
     * Compatibility overload.
     *
     * This method keeps older tests and callers working.
     * Full book -> part -> chapter validation requires
     * the five-argument method below.
     */
    public static List<String> validateQuestions(
            JSONArray questions,
            JSONArray books,
            JSONArray chapters,
            JSONArray sections
    ) {

        return validateQuestions(
                questions,
                books,
                null,
                chapters,
                sections
        );
    }

    /*
     * Full question validation.
     *
     * Hierarchy:
     * book -> bookPart -> chapter -> section -> question
     */
    public static List<String> validateQuestions(
            JSONArray questions,
            JSONArray books,
            JSONArray bookParts,
            JSONArray chapters,
            JSONArray sections
    ) {

        List<String> errors = new ArrayList<>();

        if (questions == null || questions.length() == 0) {
            errors.add("Questions array is empty.");
            return errors;
        }

        // Map: book externalId -> known book.
        Set<String> knownBookIds = new HashSet<>();

        if (books != null) {

            for (int i = 0; i < books.length(); i++) {

                JSONObject book = books.optJSONObject(i);

                if (book != null) {

                    String id = getString(book, "externalId");

                    if (id != null) {
                        knownBookIds.add(id);
                    }
                }
            }
        }

        // Map: book part externalId -> parent book externalId.
        Map<String, String> partBookMap = new HashMap<>();

        if (bookParts != null) {

            for (int i = 0; i < bookParts.length(); i++) {

                JSONObject part = bookParts.optJSONObject(i);

                if (part != null) {

                    String partId = getString(part, "externalId");
                    String bookId = getString(part, "bookExternalId");

                    if (partId != null && bookId != null) {
                        partBookMap.put(partId, bookId);
                    }
                }
            }
        }

        // Map: chapter externalId -> parent book part externalId.
        Map<String, String> chapterPartMap = new HashMap<>();

        if (chapters != null) {

            for (int i = 0; i < chapters.length(); i++) {

                JSONObject chapter = chapters.optJSONObject(i);

                if (chapter != null) {

                    String chapterId = getString(chapter, "externalId");
                    String partId = getString(chapter, "bookPartExternalId");

                    if (chapterId != null && partId != null) {
                        chapterPartMap.put(chapterId, partId);
                    }
                }
            }
        }

        // Map: section externalId -> parent chapter externalId.
        Map<String, String> sectionChapterMap = new HashMap<>();

        if (sections != null) {

            for (int i = 0; i < sections.length(); i++) {

                JSONObject section = sections.optJSONObject(i);

                if (section != null) {

                    String sectionId = getString(section, "externalId");
                    String chapterId = getString(section, "chapterExternalId");

                    if (sectionId != null && chapterId != null) {
                        sectionChapterMap.put(sectionId, chapterId);
                    }
                }
            }
        }

        Set<String> questionIds = new HashSet<>();
        Set<String> questionOrders = new HashSet<>();

        for (int i = 0; i < questions.length(); i++) {

            JSONObject question = questions.optJSONObject(i);

            if (question == null) {
                errors.add("Question at index " + i + " is not an object.");
                continue;
            }

            String externalId = getString(question, "externalId");
            String bookExternalId = getString(question, "bookExternalId");
            String chapterExternalId = getString(question, "chapterExternalId");
            String sectionExternalId = getString(question, "sectionExternalId");
            String questionText = getString(question, "questionText");
            String questionType = getString(question, "questionType");

            // externalId
            if (externalId == null) {

                errors.add(
                        "Question at index " + i
                                + ": externalId is required."
                );

            } else if (!questionIds.add(externalId)) {

                errors.add("Duplicate question externalId: " + externalId);
            }

            // bookExternalId
            if (bookExternalId == null) {

                errors.add(
                        "Question at index " + i
                                + ": bookExternalId is required."
                );

            } else if (!knownBookIds.contains(bookExternalId)) {

                errors.add(
                        "Question at index " + i
                                + " references an unknown book: "
                                + bookExternalId
                );
            }

            // chapterExternalId
            if (chapterExternalId == null) {

                errors.add(
                        "Question at index " + i
                                + ": chapterExternalId is required."
                );

            } else if (!chapterPartMap.containsKey(chapterExternalId)) {

                errors.add(
                        "Question at index " + i
                                + " references an unknown chapter: "
                                + chapterExternalId
                );

            } else if (bookParts != null) {

                // Resolve chapter -> part -> book.
                String partId = chapterPartMap.get(chapterExternalId);
                String chapterBookId = partBookMap.get(partId);

                if (chapterBookId == null) {

                    errors.add(
                            "Question at index " + i
                                    + ": chapter references a book part "
                                    + "with an unknown parent book: "
                                    + partId
                    );

                } else if (bookExternalId != null
                        && !bookExternalId.equals(chapterBookId)) {

                    errors.add(
                            "Question at index " + i
                                    + ": chapter belongs to book "
                                    + chapterBookId
                                    + ", but question references book "
                                    + bookExternalId
                    );
                }
            }

            // questionText
            if (questionText == null) {

                errors.add(
                        "Question at index " + i
                                + ": questionText is required."
                );
            }

            // questionType
            if (questionType == null) {

                errors.add(
                        "Question at index " + i
                                + ": questionType is required."
                );

            } else if (!isSupportedQuestionType(questionType)) {

                errors.add(
                        "Question at index " + i
                                + ": unsupported questionType: "
                                + questionType
                );
            }

            // Optional section. If supplied, it must belong to this chapter.
            if (sectionExternalId != null) {

                if (!sectionChapterMap.containsKey(sectionExternalId)) {

                    errors.add(
                            "Question at index " + i
                                    + " references an unknown section: "
                                    + sectionExternalId
                    );

                } else if (chapterExternalId != null
                        && !chapterExternalId.equals(
                        sectionChapterMap.get(sectionExternalId))) {

                    errors.add(
                            "Question at index " + i
                                    + ": section does not belong to chapter "
                                    + chapterExternalId
                    );
                }
            }

            // originalNumber
            validateOptionalStringOrNumber(
                    question,
                    "originalNumber",
                    i,
                    errors
            );

            // sourcePage
            validateOptionalPositiveInteger(
                    question,
                    "sourcePage",
                    i,
                    errors
            );

            // sortOrder
            Object sortValue = question.opt("sortOrder");

            if (question.has("sortOrder") && !question.isNull("sortOrder")) {

                if (!(sortValue instanceof Integer)
                        && !(sortValue instanceof Long)) {

                    errors.add(
                            "Question at index " + i
                                    + ": sortOrder must be an integer."
                    );

                } else {

                    long sortOrder = ((Number) sortValue).longValue();

                    if (sortOrder <= 0) {

                        errors.add(
                                "Question at index " + i
                                        + ": sortOrder must be greater than zero."
                        );

                    } else if (chapterExternalId != null) {

                        String parentId = chapterExternalId;

                        if (sectionExternalId != null) {
                            parentId = sectionExternalId;
                        }

                        String uniqueOrder = parentId + ":" + sortOrder;

                        if (!questionOrders.add(uniqueOrder)) {

                            errors.add(
                                    "Duplicate question sortOrder "
                                            + sortOrder
                                            + " in "
                                            + parentId
                            );
                        }
                    }
                }
            }

            // difficulty: optional integer from 0 to 5.
            validateOptionalIntegerRange(
                    question,
                    "difficulty",
                    0,
                    5,
                    i,
                    errors,
                    "Question"
            );
        }

        return errors;
    }

    // =====================================================
    // 6. QUESTION TYPE VALIDATION
    // =====================================================

    private static boolean isSupportedQuestionType(String type) {

        String normalized = type.trim().toUpperCase(Locale.ROOT);

        return normalized.equals("SINGLE_CHOICE")
                || normalized.equals("MULTIPLE_CHOICE")
                || normalized.equals("MATCHING")
                || normalized.equals("TRUE_FALSE")
                || normalized.equals("SEQUENCE")
                || normalized.equals("NUMERIC")
                || normalized.equals("CALCULATION")
                || normalized.equals("MULTI_PART")
                || normalized.equals("OPEN_TEXT")
                || normalized.equals("SHORT_ANSWER")
                || normalized.equals("FILL_IN_THE_BLANK");
    }

    // =====================================================
    // 7. COMMON HELPERS
    // =====================================================

    private static String getString(
            JSONObject object,
            String key
    ) {

        Object value = object.opt(key);

        if (!(value instanceof String)) {
            return null;
        }

        String result = ((String) value).trim();

        return result.isEmpty() ? null : result;
    }

    private static void validateOptionalString(
            JSONObject object,
            String key,
            int index,
            List<String> errors,
            String entityName
    ) {

        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        if (!(object.opt(key) instanceof String)) {

            errors.add(
                    entityName + " at index " + index
                            + ": " + key + " must be a string."
            );
        }
    }

    private static void validateOptionalStringOrNumber(
            JSONObject object,
            String key,
            int index,
            List<String> errors
    ) {

        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        Object value = object.opt(key);

        if (!(value instanceof String)
                && !(value instanceof Integer)
                && !(value instanceof Long)) {

            errors.add(
                    "Question at index " + index
                            + ": " + key + " must be a string or integer."
            );
        }
    }

    private static void validateOptionalPositiveInteger(
            JSONObject object,
            String key,
            int index,
            List<String> errors
    ) {

        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        Object value = object.opt(key);

        if (!(value instanceof Integer)
                && !(value instanceof Long)) {

            errors.add(
                    "Question at index " + index
                            + ": " + key + " must be an integer."
            );

        } else if (((Number) value).longValue() <= 0) {

            errors.add(
                    "Question at index " + index
                            + ": " + key + " must be greater than zero."
            );
        }
    }

    /**
     * Проверяет необязательное целочисленное поле
     * в заданном диапазоне включительно.
     */
    private static void validateOptionalIntegerRange(
            JSONObject object,
            String key,
            int minValue,
            int maxValue,
            int index,
            List<String> errors,
            String entityName
    ) {

        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        Object value = object.opt(key);

        if (!(value instanceof Integer)
                && !(value instanceof Long)) {

            errors.add(
                    entityName + " at index " + index
                            + ": " + key + " must be an integer."
            );

            return;
        }

        long number = ((Number) value).longValue();

        if (number < minValue || number > maxValue) {

            errors.add(
                    entityName + " at index " + index
                            + ": " + key + " must be between "
                            + minValue + " and " + maxValue + "."
            );
        }
    }
}
