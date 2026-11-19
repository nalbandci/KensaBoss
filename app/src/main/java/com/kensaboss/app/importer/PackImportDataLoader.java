
package com.kensaboss.app.importer;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PackImportDataLoader {

    private static final int MAX_OPTIONAL_JSON_SIZE =
            10 * 1024 * 1024;

    public static PackImportData load(File packageFile)
            throws IOException, JSONException {

        if (packageFile == null) {
            throw new IllegalArgumentException(
                    "Package file must not be null"
            );
        }

        // Проверяем архив и читаем manifest.json.
        PackManifest manifest =
                KbPackReader.validatePackage(packageFile);

        // Основные файлы пакета обязательны.
        JSONArray books = PackJsonReader.readArray(
                packageFile,
                "data/books.json"
        );

        JSONArray bookParts = PackJsonReader.readArray(
                packageFile,
                "data/book_parts.json"
        );

        JSONArray chapters = PackJsonReader.readArray(
                packageFile,
                "data/chapters.json"
        );

        JSONArray sections = PackJsonReader.readArray(
                packageFile,
                "data/sections.json"
        );

        JSONArray questions = PackJsonReader.readArray(
                packageFile,
                "data/questions.json"
        );

        // Дополнительные файлы необязательны.
        JSONArray answerOptions = readOptionalArray(
                packageFile,
                "data/answer_options.json"
        );

        JSONArray correctAnswers = readOptionalArray(
                packageFile,
                "data/correct_answers.json"
        );

        JSONArray explanations = readOptionalArray(
                packageFile,
                "data/explanations.json"
        );

        JSONArray explanationAssets = readOptionalArray(
                packageFile,
                "data/explanation_assets.json"
        );

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
     * Читает дополнительный JSON-файл.
     *
     * Если файл отсутствует, возвращает пустой JSONArray.
     * Если файл существует, проверяет его размер и формат.
     */
    private static JSONArray readOptionalArray(
            File packageFile,
            String entryName
    ) throws IOException, JSONException {

        try (ZipFile zipFile = new ZipFile(packageFile)) {

            ZipEntry entry = zipFile.getEntry(entryName);

            if (entry == null) {
                return new JSONArray();
            }

            if (entry.isDirectory()) {
                throw new IOException(
                        "JSON entry is a directory: " + entryName
                );
            }

            long declaredSize = entry.getSize();

            if (declaredSize > MAX_OPTIONAL_JSON_SIZE) {
                throw new IOException(
                        "JSON file is too large: " + entryName
                );
            }

            try (
                    InputStream inputStream =
                            zipFile.getInputStream(entry);

                    ByteArrayOutputStream outputStream =
                            new ByteArrayOutputStream()
            ) {

                byte[] buffer = new byte[4096];

                int bytesRead;
                int totalBytes = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    totalBytes += bytesRead;

                    if (totalBytes > MAX_OPTIONAL_JSON_SIZE) {
                        throw new IOException(
                                "JSON file exceeds size limit: "
                                        + entryName
                        );
                    }

                    outputStream.write(
                            buffer,
                            0,
                            bytesRead
                    );
                }

                String json = new String(
                        outputStream.toByteArray(),
                        StandardCharsets.UTF_8
                );

                return new JSONArray(json);
            }
        }
    }
}
