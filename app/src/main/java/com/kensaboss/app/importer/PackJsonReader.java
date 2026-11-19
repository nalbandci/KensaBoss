
package com.kensaboss.app.importer;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Читает JSON-массивы из файлов внутри .kbpack.
 *
 * Не извлекает файлы на диск и не изменяет SQLite.
 */
public class PackJsonReader {

    private static final String DATA_PREFIX = "data/";

    private static final int MAX_JSON_SIZE = 10 * 1024 * 1024;

    /**
     * Читает JSON-файл из указанного ZIP-архива.
     *
     * Например:
     * data/books.json
     * data/questions.json
     */
    public static JSONArray readArray(
            File packageFile,
            String entryName
    ) throws IOException, JSONException {

        validateArguments(packageFile, entryName);

        try (ZipFile zipFile = new ZipFile(packageFile)) {

            ZipEntry entry = zipFile.getEntry(entryName);

            if (entry == null) {
                throw new IOException(
                        "JSON file not found: " + entryName
                );
            }

            if (entry.isDirectory()) {
                throw new IOException(
                        "Expected a file, but found a directory: "
                                + entryName
                );
            }

            long declaredSize = entry.getSize();

            if (declaredSize > MAX_JSON_SIZE) {
                throw new IOException(
                        "JSON file is too large: " + entryName
                );
            }

            byte[] data = readEntry(
                    zipFile,
                    entry
            );

            String json = new String(
                    data,
                    java.nio.charset.StandardCharsets.UTF_8
            );

            JSONArray array = new JSONArray(json);

            if (array.length() == 0) {
                throw new JSONException(
                        "JSON array is empty: " + entryName
                );
            }

            return array;
        }
    }

    /**
     * Проверяет аргументы и безопасность пути.
     */
    private static void validateArguments(
            File packageFile,
            String entryName
    ) throws IOException {

        if (packageFile == null
                || !packageFile.exists()
                || !packageFile.isFile()
                || !packageFile.canRead()) {

            throw new IOException(
                    "Package file is unavailable."
            );
        }

        if (entryName == null
                || !entryName.startsWith(DATA_PREFIX)
                || !entryName.endsWith(".json")
                || entryName.contains("\\")
                || entryName.startsWith("/")
                || entryName.contains("\0")) {

            throw new IOException(
                    "Invalid JSON entry path."
            );
        }

        String[] parts = entryName.split("/");

        for (String part : parts) {

            if ("..".equals(part) || part.isEmpty()) {
                throw new IOException(
                        "Unsafe JSON entry path."
                );
            }
        }
    }

    /**
     * Читает содержимое ZIP-записи с ограничением размера.
     */
    private static byte[] readEntry(
            ZipFile zipFile,
            ZipEntry entry
    ) throws IOException {

        try (
                InputStream inputStream =
                        zipFile.getInputStream(entry);

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            byte[] buffer = new byte[8192];

            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                totalBytes += bytesRead;

                if (totalBytes > MAX_JSON_SIZE) {
                    throw new IOException(
                            "JSON file exceeds the size limit."
                    );
                }

                outputStream.write(
                        buffer,
                        0,
                        bytesRead
                );
            }

            if (totalBytes == 0) {
                throw new IOException(
                        "JSON file is empty."
                );
            }

            return outputStream.toByteArray();
        }
    }
}
