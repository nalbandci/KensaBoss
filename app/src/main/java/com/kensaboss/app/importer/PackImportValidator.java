package com.kensaboss.app.importer;

import java.util.List;

/**
 * Проверяет все данные импортируемого пакета
 * перед сохранением в базу данных.
 *
 * Класс не изменяет SQLite.
 */
public final class PackImportValidator {

    private PackImportValidator() {
    }

    public static PackImportResult validate(PackImportData data) {

        PackImportResult result = new PackImportResult();

        if (data == null) {
            result.addError("Import data is null.");
            result.setStatus(PackImportResult.STATUS_FAILED);
            return result;
        }

        if (data.getManifest() == null) {
            result.addError("Package manifest is missing.");
        }

        // 1. Книги
        addErrors(
                result,
                PackDataValidator.validateBooks(data.getBooks())
        );

        // 2. Части книг
        addErrors(
                result,
                PackDataValidator.validateBookParts(
                        data.getBookParts(),
                        data.getBooks()
                )
        );

        // 3. Главы
        addErrors(
                result,
                PackDataValidator.validateChapters(
                        data.getChapters(),
                        data.getBookParts()
                )
        );

        // 4. Разделы
        addErrors(
                result,
                PackDataValidator.validateSections(
                        data.getSections(),
                        data.getChapters()
                )
        );

        // 5. Вопросы
        addErrors(
                result,
                PackDataValidator.validateQuestions(
                        data.getQuestions(),
                        data.getBooks(),
                        data.getBookParts(),
                        data.getChapters(),
                        data.getSections()
                )
        );

        // 6. Варианты ответов
        addErrors(
                result,
                PackExtendedDataValidator.validateAnswerOptions(
                        data.getAnswerOptions(),
                        data.getQuestions()
                )
        );

        // 7. Правильные ответы
        addErrors(
                result,
                PackExtendedDataValidator.validateCorrectAnswers(
                        data.getCorrectAnswers(),
                        data.getQuestions(),
                        data.getAnswerOptions()
                )
        );

        // 8. Объяснения
        addErrors(
                result,
                PackExtendedDataValidator.validateExplanations(
                        data.getExplanations(),
                        data.getQuestions()
                )
        );

        // 9. Изображения, схемы и другие материалы
        addErrors(
                result,
                PackExtendedDataValidator.validateExplanationAssets(
                        data.getExplanationAssets(),
                        data.getExplanations()
                )
        );

        // 10. Общий результат
        if (result.hasErrors()) {
            result.setStatus(PackImportResult.STATUS_FAILED);
        } else {
            result.setStatus(PackImportResult.STATUS_SUCCESS);
        }

        return result;
    }

    private static void addErrors(
            PackImportResult result,
            List<String> errors
    ) {

        if (errors == null || errors.isEmpty()) {
            return;
        }

        for (String error : errors) {
            if (error != null && !error.trim().isEmpty()) {
                result.addError(error);
            }
        }
    }
}