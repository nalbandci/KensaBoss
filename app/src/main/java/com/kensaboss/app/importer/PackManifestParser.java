
package com.kensaboss.app.importer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Читает и проверяет manifest.json учебного пакета KensaBoss.
 */
public class PackManifestParser {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    /**
     * Преобразует JSON-текст в объект PackManifest.
     */
    public static PackManifest parse(String json) throws JSONException {

        if (json == null || json.trim().isEmpty()) {
            throw new JSONException("Manifest JSON is empty.");
        }

        JSONObject object = new JSONObject(json);

        int schemaVersion = getRequiredInt(
                object,
                "schemaVersion"
        );

        if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            throw new JSONException(
                    "Unsupported schemaVersion: " + schemaVersion
            );
        }

        String packUid = getRequiredString(
                object,
                "packUid"
        );

        String title = getRequiredString(
                object,
                "title"
        );

        String language = getRequiredString(
                object,
                "language"
        );

        int version = getRequiredInt(
                object,
                "version"
        );

        if (version <= 0) {
            throw new JSONException(
                    "Package version must be greater than zero."
            );
        }

        String description = object.optString(
                "description",
                ""
        );

        String createdAt = object.optString(
                "createdAt",
                ""
        );

        return new PackManifest(
                schemaVersion,
                packUid,
                title,
                language,
                version,
                description,
                createdAt
        );
    }

    /**
     * Получает обязательную непустую строку.
     */
    private static String getRequiredString(
            JSONObject object,
            String key
    ) throws JSONException {

        if (!object.has(key) || object.isNull(key)) {
            throw new JSONException(
                    "Missing required field: " + key
            );
        }

        String value = object.getString(key).trim();

        if (value.isEmpty()) {
            throw new JSONException(
                    "Field cannot be empty: " + key
            );
        }

        return value;
    }

    /**
     * Получает обязательное целое число.
     */
    private static int getRequiredInt(
            JSONObject object,
            String key
    ) throws JSONException {

        if (!object.has(key) || object.isNull(key)) {
            throw new JSONException(
                    "Missing required field: " + key
            );
        }

        return object.getInt(key);
    }
}
