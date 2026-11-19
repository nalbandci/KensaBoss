
package com.kensaboss.app.importer;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Читает ZIP-архивы KensaBoss (.kbpack).
 *
 * На данном этапе извлекает только manifest.json.
 */
public class KbPackArchiveReader {

    // Максимальный размер manifest.json: 1 МБ.
    private static final int MAX_MANIFEST_SIZE =
            1024 * 1024;

    // Максимальный общий распакованный размер архива: 200 МБ.
    private static final long MAX_TOTAL_SIZE =
            200L * 1024L * 1024L;

    // Максимальное количество файлов в архиве.
    private static final int MAX_ENTRIES = 5000;

    private final Context context;

    public KbPackArchiveReader(Context context) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "Context cannot be null."
            );
        }

        this.context = context.getApplicationContext();
    }

    /**
     * Открывает архив и читает manifest.json.
     *
     * @param archiveUri URI выбранного файла .kbpack
     * @return проверенный объект PackManifest
     */
    public PackManifest readManifest(
            Uri archiveUri
    ) throws IOException, JSONException {

        if (archiveUri == null) {
            throw new IllegalArgumentException(
                    "Archive URI cannot be null."
            );
        }

        InputStream inputStream =
                context.getContentResolver()
                        .openInputStream(archiveUri);

        if (inputStream == null) {
            throw new IOException(
                    "Cannot open selected archive."
            );
        }

        byte[] manifestBytes = null;

        long totalSize = 0;
        int entryCount = 0;

        try (
                ZipInputStream zipInputStream =
                        new ZipInputStream(inputStream)
        ) {

            ZipEntry entry;

            byte[] buffer = new byte[8192];

            while ((entry = zipInputStream.getNextEntry())
                    != null) {

                entryCount++;

                if (entryCount > MAX_ENTRIES) {
                    throw new IOException(
                            "Archive contains too many entries."
                    );
                }

                String entryName = entry.getName();

                boolean isManifest =
                        "manifest.json".equals(entryName);

                if (isManifest && manifestBytes != null) {
                    throw new IOException(
                            "Archive contains duplicate manifest.json."
                    );
                }

                ByteArrayOutputStream manifestOutput =
                        isManifest
                                ? new ByteArrayOutputStream()
                                : null;

                int bytesRead;

                while ((bytesRead =
                        zipInputStream.read(buffer)) != -1) {

                    totalSize += bytesRead;

                    if (totalSize > MAX_TOTAL_SIZE) {
                        throw new IOException(
                                "Archive exceeds maximum unpacked size."
                        );
                    }

                    if (isManifest) {

                        if (manifestOutput.size() + bytesRead
                                > MAX_MANIFEST_SIZE) {

                            throw new IOException(
                                    "manifest.json is too large."
                            );
                        }

                        manifestOutput.write(
                                buffer,
                                0,
                                bytesRead
                        );
                    }
                }

                if (isManifest) {
                    manifestBytes =
                            manifestOutput.toByteArray();
                }

                zipInputStream.closeEntry();
            }
        }

        if (manifestBytes == null) {
            throw new IOException(
                    "manifest.json was not found in archive."
            );
        }

        String json = new String(
                manifestBytes,
                StandardCharsets.UTF_8
        );

        return PackManifestParser.parse(json);
    }
}
