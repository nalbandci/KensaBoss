
package com.kensaboss.app.importer;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Читает и предварительно проверяет ZIP-пакеты KensaBoss.
 *
 * На этом этапе класс не изменяет базу данных
 * и не извлекает файлы на диск.
 */
public class KbPackReader {

    private static final String MANIFEST_PATH = "manifest.json";
    private static final String DATA_PREFIX = "data/";

    private static final int MAX_ENTRIES = 10000;
    private static final int MAX_MANIFEST_SIZE = 1024 * 1024;

    /**
     * Проверяет архив и возвращает его паспорт.
     */
    public static PackManifest validatePackage(File packageFile)
            throws IOException, JSONException {

        if (packageFile == null) {
            throw new IOException("Package file is null.");
        }

        if (!packageFile.exists()) {
            throw new IOException("Package file does not exist.");
        }

        if (!packageFile.isFile()) {
            throw new IOException("Selected path is not a file.");
        }

        if (!packageFile.canRead()) {
            throw new IOException("Package file cannot be read.");
        }

        if (packageFile.length() == 0) {
            throw new IOException("Package file is empty.");
        }

        try (ZipFile zipFile = new ZipFile(packageFile)) {

            Enumeration<? extends ZipEntry> entries =
                    zipFile.entries();

            Set<String> entryNames = new HashSet<>();

            ZipEntry manifestEntry = null;

            boolean hasDataFiles = false;

            int entryCount = 0;

            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();

                entryCount++;

                if (entryCount > MAX_ENTRIES) {
                    throw new IOException(
                            "Package contains too many entries."
                    );
                }

                String entryName = entry.getName();

                validateEntryName(entryName);

                if (!entryNames.add(entryName)) {
                    throw new IOException(
                            "Duplicate ZIP entry: " + entryName
                    );
                }

                if (MANIFEST_PATH.equals(entryName)) {

                    if (entry.isDirectory()) {
                        throw new IOException(
                                "manifest.json cannot be a directory."
                        );
                    }

                    manifestEntry = entry;
                }

                if (!entry.isDirectory()
                        && entryName.startsWith(DATA_PREFIX)) {

                    hasDataFiles = true;
                }
            }

            if (manifestEntry == null) {
                throw new IOException(
                        "Package does not contain manifest.json."
                );
            }

            if (!hasDataFiles) {
                throw new IOException(
                        "Package does not contain files in data/."
                );
            }

            String manifestJson = readManifest(
                    zipFile,
                    manifestEntry
            );

            return PackManifestParser.parse(manifestJson);
        }
    }

    /**
     * Проверяет безопасность имени ZIP-записи.
     *
     * Запрещает абсолютные пути и переходы
     * в родительские директории.
     */
    private static void validateEntryName(String entryName)
            throws IOException {

        if (entryName == null || entryName.isEmpty()) {
            throw new IOException("ZIP entry has an empty name.");
        }

        if (entryName.startsWith("/")
                || entryName.startsWith("\\")
                || entryName.contains("\\")) {

            throw new IOException(
                    "Unsafe ZIP entry path: " + entryName
            );
        }

        if (entryName.indexOf('\0') >= 0) {
            throw new IOException(
                    "ZIP entry contains a null character."
            );
        }

        String[] pathParts = entryName.split("/");

        for (String part : pathParts) {

            if ("..".equals(part)) {
                throw new IOException(
                        "ZIP entry contains parent directory traversal: "
                                + entryName
                );
            }
        }
    }

    /**
     * Читает manifest.json с ограничением размера.
     */
    private static String readManifest(
            ZipFile zipFile,
            ZipEntry manifestEntry
    ) throws IOException {

        long declaredSize = manifestEntry.getSize();

        if (declaredSize > MAX_MANIFEST_SIZE) {
            throw new IOException(
                    "manifest.json is too large."
            );
        }

        try (
                InputStream inputStream =
                        zipFile.getInputStream(manifestEntry);

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            byte[] buffer = new byte[4096];

            int bytesRead;

            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                totalBytes += bytesRead;

                if (totalBytes > MAX_MANIFEST_SIZE) {
                    throw new IOException(
                            "manifest.json exceeds the size limit."
                    );
                }

                outputStream.write(buffer, 0, bytesRead);
            }

            if (totalBytes == 0) {
                throw new IOException(
                        "manifest.json is empty."
                );
            }

            return new String(
                    outputStream.toByteArray(),
                    StandardCharsets.UTF_8
            );
        }
    }
}
