
package com.kensaboss.app.importer;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Загружает все данные из пакета KensaBoss.
 *
 * Не изменяет SQLite.
 * Не извлекает файлы на диск.
 *
 * Результат — готовый объект PackImportData.
 */
public final class PackImportDataReader {

    private static final String DATA_PREFIX = "data/";

    private static final String[] REQUIRED_FILES = {
            "books.json",
            "book_parts.json",
            "chapters.json",
            "sections.json",
            "questions.json"
    };

    private static final String[] OPTIONAL_FILES = {
            "answer_options.json",
            "correct_answers.json",
            "explanations.json",
            "explanation_assets.json"
    };

    private PackImportDataReader() {
    }

    /**
     * Читает и подготавливает все данные пакета.
     */
    public static PackImportData read(File packageFile)
            throws IOException, JSONException {

        // 1. Проверяем архив и читаем manifest.json.

        PackManifest manifest =
                KbPackReader.validatePackage(packageFile);

        // 2. Проверяем наличие обязательных файлов.

        validateRequiredFiles(packageFile);

        // 3. Читаем основные данные.

        JSONArray books = readRequiredArray(
                packageFile,
                "books.json"
        );

        JSONArray bookParts = readRequiredArray(
                packageFile,
                "book_parts.json"
        );

        JSONArray chapters = readRequiredArray(
                packageFile,
                "chapters.json"
        );

        JSONArray sections = readRequiredArray(
                packageFile,
                "sections.json"
        );

        JSONArray questions = readRequiredArray(
                packageFile,
                "questions.json"
        );

        // 4. Читаем дополнительные данные.
        // Отсутствующие дополнительные файлы
        // считаются пустыми массивами.

        JSONArray answerOptions = readOptionalArray(
                packageFile,
                "answer_options.json"
        );

        JSONArray correctAnswers = readOptionalArray(
                packageFile,
                "correct_answers.json"
        );

        JSONArray explanations = readOptionalArray(
                packageFile,
                "explanations.json"
        );

        JSONArray explanationAssets = readOptionalArray(
                packageFile,
                "explanation_assets.json"
        );

        // 5. Собираем единый объект.

        return new PackImportData(
                manifest,
                books,
                bookParts,
                chapters,
                sections,
                questions,
                answerOptions,
                correctAnswers,
                explanations,
                explanationAssets
        );
    }

    /**
     * Проверяет наличие обязательных файлов.
     */
    private static void validateRequiredFiles(File packageFile)
            throws IOException {

        try (ZipFile zipFile = new ZipFile(packageFile)) {

            for (String fileName : REQUIRED_FILES) {

                String entryName = DATA_PREFIX + fileName;

                ZipEntry entry = zipFile.getEntry(entryName);

                if (entry == null) {
                    throw new IOException(
                            "Required package file is missing: "
                                    + entryName
                    );
                }

                if (entry.isDirectory()) {
                    throw new IOException(
                            "Expected a file, but found a directory: "
                                    + entryName
                    );
                }
            }
        }
    }

    /**
     * Читает обязательный JSON-массив.
     */
    private static JSONArray readRequiredArray(
            File packageFile,
            String fileName
    ) throws IOException, JSONException {

        String entryName = DATA_PREFIX + fileName;

        return PackJsonReader.readArray(
                packageFile,
                entryName
        );
    }

    /**
     * Читает необязательный JSON-массив.
     *
     * Если файла нет, возвращает пустой JSONArray.
     */
    private static JSONArray readOptionalArray(
            File packageFile,
            String fileName
    ) throws IOException, JSONException {

        String entryName = DATA_PREFIX + fileName;

        try (ZipFile zipFile = new ZipFile(packageFile)) {

            ZipEntry entry = zipFile.getEntry(entryName);

            if (entry == null) {
                return new JSONArray();
            }

            if (entry.isDirectory()) {
                throw new IOException(
                        "Expected a file, but found a directory: "
                                + entryName
                );
            }
        }

        return PackJsonReader.readArray(
                packageFile,
                entryName
        );
    }
}
