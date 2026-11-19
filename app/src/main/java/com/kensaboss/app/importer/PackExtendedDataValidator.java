
package com.kensaboss.app.importer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PackExtendedDataValidator {

    private static final Set<String> VERIFICATION_STATUSES = new HashSet<>();

    static {
        VERIFICATION_STATUSES.add("VERIFIED");
        VERIFICATION_STATUSES.add("NOT_VERIFIED");
        VERIFICATION_STATUSES.add("AMBIGUOUS");
        VERIFICATION_STATUSES.add("CONFLICTING");
        VERIFICATION_STATUSES.add("PRINTING_ERROR");
    }

    private PackExtendedDataValidator() {
    }

    // =====================================================
    // 1. ANSWER OPTIONS
    // =====================================================

    public static List<String> validateAnswerOptions(
            JSONArray options,
            JSONArray questions
    ) {
        List<String> errors = new ArrayList<>();

        if (options == null || options.length() == 0) {
            return errors;
        }

        Set<String> knownQuestionIds =
                collectExternalIds(questions, "externalId");

        Set<String> optionIds = new HashSet<>();
        Set<String> optionOrders = new HashSet<>();

        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.optJSONObject(i);

            if (option == null) {
                errors.add("Answer option at index " + i
                        + " is not an object.");
                continue;
            }

            String externalId = getString(option, "externalId");
            String questionId = getString(option, "questionExternalId");
            String optionText = getString(option, "optionText");

            if (externalId == null) {
                errors.add("Answer option at index " + i
                        + ": externalId is required.");
            } else if (!optionIds.add(externalId)) {
                errors.add("Duplicate answer option externalId: " + externalId);
            }

            if (questionId == null) {
                errors.add("Answer option at index " + i
                        + ": questionExternalId is required.");
            } else if (!knownQuestionIds.contains(questionId)) {
                errors.add("Answer option at index " + i
                        + " references an unknown question: " + questionId);
            }

            if (optionText == null) {
                errors.add("Answer option at index " + i
                        + ": optionText is required.");
            }

            validateOptionalString(
                    option, "optionLabel", i, errors, "Answer option"
            );

            Object sortValue = option.opt("sortOrder");

            if (!isInteger(sortValue)) {
                errors.add("Answer option at index " + i
                        + ": sortOrder must be an integer.");
            } else {
                long order = ((Number) sortValue).longValue();

                if (order <= 0) {
                    errors.add("Answer option at index " + i
                            + ": sortOrder must be greater than zero.");
                } else if (questionId != null) {
                    String uniqueOrder = questionId + ":" + order;

                    if (!optionOrders.add(uniqueOrder)) {
                        errors.add("Duplicate answer option sortOrder "
                                + order + " in question " + questionId);
                    }
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 2. CORRECT ANSWERS
    // =====================================================

    public static List<String> validateCorrectAnswers(
            JSONArray correctAnswers,
            JSONArray questions,
            JSONArray options
    ) {
        List<String> errors = new ArrayList<>();

        if (correctAnswers == null || correctAnswers.length() == 0) {
            return errors;
        }

        Map<String, String> questionTypes = new HashMap<>();

        if (questions != null) {
            for (int i = 0; i < questions.length(); i++) {
                JSONObject question = questions.optJSONObject(i);

                if (question == null) {
                    continue;
                }

                String id = getString(question, "externalId");
                String type = getString(question, "questionType");

                if (id != null && type != null) {
                    questionTypes.put(id, type.toUpperCase(Locale.ROOT));
                }
            }
        }

        Map<String, String> optionQuestionMap = new HashMap<>();

        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.optJSONObject(i);

                if (option == null) {
                    continue;
                }

                String optionId = getString(option, "externalId");
                String questionId = getString(option, "questionExternalId");

                if (optionId != null && questionId != null) {
                    optionQuestionMap.put(optionId, questionId);
                }
            }
        }

        Set<String> uniqueAnswers = new HashSet<>();

        for (int i = 0; i < correctAnswers.length(); i++) {
            JSONObject answer = correctAnswers.optJSONObject(i);

            if (answer == null) {
                errors.add("Correct answer at index " + i
                        + " is not an object.");
                continue;
            }

            String questionId = getString(answer, "questionExternalId");
            String optionId = getString(answer, "optionExternalId");

            boolean hasOptionId = optionId != null;

            Object answerValue = answer.opt("answerValue");

            boolean hasAnswerValue =
                    answer.has("answerValue")
                            && !answer.isNull("answerValue")
                            && isValidAnswerValue(answerValue);

            if (questionId == null) {
                errors.add("Correct answer at index " + i
                        + ": questionExternalId is required.");
            } else if (!questionTypes.containsKey(questionId)) {
                errors.add("Correct answer at index " + i
                        + " references an unknown question: " + questionId);
            }

            if (hasOptionId == hasAnswerValue) {
                errors.add("Correct answer at index " + i
                        + ": exactly one of optionExternalId or answerValue "
                        + "must be provided.");
            }

            if (hasOptionId) {
                String optionQuestionId = optionQuestionMap.get(optionId);

                if (optionQuestionId == null) {
                    errors.add("Correct answer at index " + i
                            + " references an unknown option: " + optionId);
                } else if (questionId != null
                        && !questionId.equals(optionQuestionId)) {
                    errors.add("Correct answer at index " + i
                            + ": option " + optionId
                            + " belongs to another question.");
                }
            }

            if (answer.has("answerValue")
                    && !answer.isNull("answerValue")
                    && !hasAnswerValue) {
                errors.add("Correct answer at index " + i
                        + ": answerValue must be a non-empty string "
                        + "or a finite number.");
            }

            validateOptionalPrimitive(
                    answer, "sourceValue", i, errors, "Correct answer"
            );

            String status = getString(answer, "verificationStatus");

            if (status != null
                    && !VERIFICATION_STATUSES.contains(
                    status.toUpperCase(Locale.ROOT))) {
                errors.add("Correct answer at index " + i
                        + ": unsupported verificationStatus: " + status);
            }

            validateOptionalString(
                    answer,
                    "verificationNotes",
                    i,
                    errors,
                    "Correct answer"
            );

            validateOptionalString(
                    answer,
                    "verifiedAt",
                    i,
                    errors,
                    "Correct answer"
            );

            if (questionId != null
                    && (hasOptionId || hasAnswerValue)) {

                String answerIdentity;

                if (hasOptionId) {
                    answerIdentity = "OPTION:" + optionId;
                } else {
                    answerIdentity = normalizeAnswerValue(answerValue);
                }

                String uniqueKey = questionId + ":" + answerIdentity;

                if (!uniqueAnswers.add(uniqueKey)) {
                    errors.add("Duplicate correct answer for question "
                            + questionId + ": " + answerIdentity);
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 3. EXPLANATIONS
    // =====================================================

    public static List<String> validateExplanations(
            JSONArray explanations,
            JSONArray questions
    ) {
        List<String> errors = new ArrayList<>();

        if (explanations == null || explanations.length() == 0) {
            return errors;
        }

        Set<String> knownQuestionIds =
                collectExternalIds(questions, "externalId");

        Set<String> uniqueExplanations = new HashSet<>();

        for (int i = 0; i < explanations.length(); i++) {
            JSONObject explanation = explanations.optJSONObject(i);

            if (explanation == null) {
                errors.add("Explanation at index " + i + " is not an object.");
                continue;
            }

            String questionId =
                    getString(explanation, "questionExternalId");

            String language = getString(explanation, "language");

            if (questionId == null) {
                errors.add("Explanation at index " + i
                        + ": questionExternalId is required.");
            } else if (!knownQuestionIds.contains(questionId)) {
                errors.add("Explanation at index " + i
                        + " references an unknown question: " + questionId);
            }

            if (language == null) {
                errors.add("Explanation at index " + i
                        + ": language is required.");
            }

            validateOptionalString(
                    explanation, "introduction", i, errors, "Explanation"
            );

            validateOptionalString(
                    explanation, "detailedText", i, errors, "Explanation"
            );

            validateOptionalString(
                    explanation, "solutionSteps", i, errors, "Explanation"
            );

            validateOptionalString(
                    explanation, "additionalNotes", i, errors, "Explanation"
            );

            validateOptionalPositiveInteger(
                    explanation, "version", i, errors, "Explanation"
            );

            boolean hasContent =
                    hasNonEmptyString(explanation, "introduction")
                            || hasNonEmptyString(explanation, "detailedText")
                            || hasNonEmptyString(explanation, "solutionSteps")
                            || hasNonEmptyString(explanation, "additionalNotes");

            if (!hasContent) {
                errors.add("Explanation at index " + i
                        + ": at least one explanation text field is required.");
            }

            if (questionId != null && language != null) {
                String key = explanationKey(questionId, language);

                if (!uniqueExplanations.add(key)) {
                    errors.add("Duplicate explanation for question "
                            + questionId + " and language " + language);
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 4. EXPLANATION ASSETS
    // =====================================================

    public static List<String> validateExplanationAssets(
            JSONArray assets,
            JSONArray explanations
    ) {
        List<String> errors = new ArrayList<>();

        if (assets == null || assets.length() == 0) {
            return errors;
        }

        Set<String> knownExplanations = new HashSet<>();

        if (explanations != null) {
            for (int i = 0; i < explanations.length(); i++) {
                JSONObject explanation = explanations.optJSONObject(i);

                if (explanation == null) {
                    continue;
                }

                String questionId =
                        getString(explanation, "questionExternalId");

                String language = getString(explanation, "language");

                if (questionId != null && language != null) {
                    knownExplanations.add(
                            explanationKey(questionId, language)
                    );
                }
            }
        }

        Set<String> uniqueAssets = new HashSet<>();

        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);

            if (asset == null) {
                errors.add("Explanation asset at index " + i
                        + " is not an object.");
                continue;
            }

            String questionId =
                    getString(asset, "questionExternalId");

            String language = getString(asset, "language");
            String assetType = getString(asset, "assetType");
            String filePath = getString(asset, "filePath");

            if (questionId == null) {
                errors.add("Explanation asset at index " + i
                        + ": questionExternalId is required.");
            }

            if (language == null) {
                errors.add("Explanation asset at index " + i
                        + ": language is required.");
            }

            if (questionId != null && language != null
                    && !knownExplanations.contains(
                    explanationKey(questionId, language))) {
                errors.add("Explanation asset at index " + i
                        + " references a missing explanation for question "
                        + questionId + " and language " + language);
            }

            if (assetType == null) {
                errors.add("Explanation asset at index " + i
                        + ": assetType is required.");
            } else if (!isSupportedAssetType(assetType)) {
                errors.add("Explanation asset at index " + i
                        + ": unsupported assetType: " + assetType);
            }

            if (filePath == null) {
                errors.add("Explanation asset at index " + i
                        + ": filePath is required.");
            } else if (!isSafeRelativePath(filePath)) {
                errors.add("Explanation asset at index " + i
                        + ": filePath must be a safe relative path.");
            }

            validateOptionalString(
                    asset, "description", i, errors, "Explanation asset"
            );

            validateOptionalPositiveInteger(
                    asset, "sortOrder", i, errors, "Explanation asset"
            );

            if (questionId != null && language != null
                    && filePath != null) {

                String key = explanationKey(questionId, language)
                        + ":" + filePath;

                if (!uniqueAssets.add(key)) {
                    errors.add("Duplicate explanation asset: " + filePath
                            + " for question " + questionId);
                }
            }
        }

        return errors;
    }

    // =====================================================
    // 5. COMMON HELPERS
    // =====================================================

    private static Set<String> collectExternalIds(
            JSONArray array,
            String field
    ) {
        Set<String> result = new HashSet<>();

        if (array == null) {
            return result;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);

            if (object == null) {
                continue;
            }

            String id = getString(object, field);

            if (id != null) {
                result.add(id);
            }
        }

        return result;
    }

    private static String getString(JSONObject object, String key) {
        Object value = object.opt(key);

        if (!(value instanceof String)) {
            return null;
        }

        String result = ((String) value).trim();

        return result.isEmpty() ? null : result;
    }

    private static boolean isInteger(Object value) {
        return value instanceof Integer || value instanceof Long;
    }

    private static boolean isPrimitiveAnswerValue(Object value) {
        if (value instanceof String
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Double
                || value instanceof Float
                || value instanceof Boolean) {

            if (value instanceof Double) {
                return Double.isFinite((Double) value);
            }

            if (value instanceof Float) {
                return Float.isFinite((Float) value);
            }

            return true;
        }

        return false;
    }

    private static boolean isValidAnswerValue(Object value) {
        if (value instanceof String) {
            return !((String) value).trim().isEmpty();
        }

        if (value instanceof Integer || value instanceof Long) {
            return true;
        }

        if (value instanceof Double) {
            return Double.isFinite((Double) value);
        }

        if (value instanceof Float) {
            return Float.isFinite((Float) value);
        }

        return false;
    }

    /**
     * Создаёт единое представление ответа для проверки дубликатов.
     *
     * Числа сравниваются математически:
     * 1, 1.0 и 1.00 превращаются в NUMBER:1.
     *
     * Строки остаются строками и не смешиваются с числами.
     */
    private static String normalizeAnswerValue(Object value) {
        if (value instanceof Number) {
            try {
                BigDecimal number = new BigDecimal(value.toString())
                        .stripTrailingZeros();

                return "NUMBER:" + number.toPlainString();

            } catch (NumberFormatException exception) {
                return "VALUE:" + String.valueOf(value);
            }
        }

        return "VALUE:" + String.valueOf(value);
    }

    private static void validateOptionalPrimitive(
            JSONObject object,
            String key,
            int index,
            List<String> errors,
            String entity
    ) {
        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        if (!isPrimitiveAnswerValue(object.opt(key))) {
            errors.add(entity + " at index " + index
                    + ": " + key
                    + " must be a string, number or boolean.");
        }
    }

    private static void validateOptionalString(
            JSONObject object,
            String key,
            int index,
            List<String> errors,
            String entity
    ) {
        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        if (!(object.opt(key) instanceof String)) {
            errors.add(entity + " at index " + index
                    + ": " + key + " must be a string.");
        }
    }

    private static void validateOptionalPositiveInteger(
            JSONObject object,
            String key,
            int index,
            List<String> errors,
            String entity
    ) {
        if (!object.has(key) || object.isNull(key)) {
            return;
        }

        Object value = object.opt(key);

        if (!isInteger(value)) {
            errors.add(entity + " at index " + index
                    + ": " + key + " must be an integer.");
        } else if (((Number) value).longValue() <= 0) {
            errors.add(entity + " at index " + index
                    + ": " + key + " must be greater than zero.");
        }
    }

    private static boolean hasNonEmptyString(
            JSONObject object,
            String key
    ) {
        return getString(object, key) != null;
    }

    private static String explanationKey(
            String questionId,
            String language
    ) {
        return questionId + ":" + language.toLowerCase(Locale.ROOT);
    }

    private static boolean isSupportedAssetType(String type) {
        String normalized = type.trim().toUpperCase(Locale.ROOT);

        return normalized.equals("IMAGE")
                || normalized.equals("DIAGRAM")
                || normalized.equals("TABLE")
                || normalized.equals("FORMULA")
                || normalized.equals("ILLUSTRATION")
                || normalized.equals("OTHER");
    }

    private static boolean isSafeRelativePath(String path) {
        String normalized = path.trim();

        if (normalized.isEmpty()
                || normalized.startsWith("/")
                || normalized.startsWith("\\")
                || normalized.contains("\\")
                || normalized.contains("\u0000")) {
            return false;
        }

        String[] parts = normalized.split("/");

        for (String part : parts) {
            if (part.isEmpty()
                    || part.equals(".")
                    || part.equals("..")) {
                return false;
            }
        }

        return true;
    }
}
