
package com.kensaboss.app.importer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackAssetExtractor {

    private static final String ZIP_ASSET_PREFIX = "assets/";
    private static final String STORAGE_DIRECTORY = "content_packs";

    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_ASSET_COUNT = 1000;

    private PackAssetExtractor() {
    }

    public static Map<String, String> extractAssets(
            Context context,
            File packageFile,
            String packUid,
            JSONArray assets
    ) throws IOException {

        Map<String, String> result = new LinkedHashMap<>();

        if (context == null) {
            throw new IllegalArgumentException(
                    "Context must not be null"
            );
        }

        if (packageFile == null
                || !packageFile.isFile()
                || !packageFile.canRead()) {

            throw new IOException(
                    "Package file is missing or unreadable"
            );
        }

        if (packUid == null || packUid.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "packUid must not be empty"
            );
        }

        if (assets == null) {
            throw new IllegalArgumentException(
                    "Assets array must not be null"
            );
        }

        if (assets.length() > MAX_ASSET_COUNT) {
            throw new IOException(
                    "Too many explanation assets"
            );
        }

        if (assets.length() == 0) {
            return result;
        }

        File root = new File(
                context.getFilesDir(),
                STORAGE_DIRECTORY
        );

        if (!root.exists() && !root.mkdirs()) {
            throw new IOException(
                    "Cannot create asset storage directory"
            );
        }

        if (!root.isDirectory()) {
            throw new IOException(
                    "Asset storage path is not a directory"
            );
        }

        String directoryName = createSafeDirectoryName(packUid);

        File canonicalRoot = root.getCanonicalFile();

        File targetDirectory = new File(
                canonicalRoot,
                directoryName
        );

        if (targetDirectory.exists()) {
            throw new IOException(
                    "Asset directory already exists for this pack"
            );
        }

        if (!targetDirectory.mkdirs()) {
            throw new IOException(
                    "Cannot create pack asset directory"
            );
        }

        Set<String> uniquePaths = new HashSet<>();

        try (ZipFile zipFile = new ZipFile(packageFile)) {

            for (int i = 0; i < assets.length(); i++) {

                JSONObject asset = assets.optJSONObject(i);

                if (asset == null) {
                    throw new IOException(
                            "Asset at index " + i + " is not an object"
                    );
                }

                Object rawPath = asset.opt("filePath");

                if (!(rawPath instanceof String)) {
                    throw new IOException(
                            "Asset filePath is missing at index " + i
                    );
                }

                String relativePath = ((String) rawPath).trim();

                validateRelativePath(relativePath);

                if (!uniquePaths.add(relativePath)) {
                    throw new IOException(
                            "Duplicate asset path: " + relativePath
                    );
                }

                String zipPath =
                        ZIP_ASSET_PREFIX + relativePath;

                ZipEntry entry = zipFile.getEntry(zipPath);

                if (entry == null || entry.isDirectory()) {
                    throw new IOException(
                            "Asset not found in package: " + zipPath
                    );
                }

                File outputFile = new File(
                        targetDirectory,
                        relativePath
                );

                File canonicalOutput = outputFile.getCanonicalFile();

                String targetPrefix =
                        targetDirectory.getCanonicalPath()
                                + File.separator;

                if (!canonicalOutput.getPath().startsWith(targetPrefix)) {
                    throw new IOException(
                            "Asset path escapes storage directory"
                    );
                }

                File parent = canonicalOutput.getParentFile();

                if (parent == null
                        || (!parent.exists() && !parent.mkdirs())) {

                    throw new IOException(
                            "Cannot create asset subdirectory"
                    );
                }

                if (!parent.isDirectory()) {
                    throw new IOException(
                            "Asset parent path is not a directory"
                    );
                }

                copyEntry(
                        zipFile,
                        entry,
                        canonicalOutput
                );

                String storedPath =
                        STORAGE_DIRECTORY
                                + "/"
                                + directoryName
                                + "/"
                                + relativePath;

                result.put(relativePath, storedPath);
            }

            return result;

        } catch (Exception exception) {

            deleteRecursively(targetDirectory);

            if (exception instanceof IOException) {
                throw (IOException) exception;
            }

            throw new IOException(
                    "Asset extraction failed",
                    exception
            );
        }
    }

    private static void copyEntry(
            ZipFile zipFile,
            ZipEntry entry,
            File outputFile
    ) throws IOException {

        try (
                InputStream input = new BufferedInputStream(
                        zipFile.getInputStream(entry)
                );

                BufferedOutputStream output =
                        new BufferedOutputStream(
                                new FileOutputStream(outputFile)
                        )
        ) {

            byte[] buffer = new byte[BUFFER_SIZE];

            int read;

            while ((read = input.read(buffer)) != -1) {

                output.write(buffer, 0, read);
            }

            output.flush();
        }
    }

    private static void validateRelativePath(
            String path
    ) throws IOException {

        if (path == null
                || path.isEmpty()
                || path.startsWith("/")
                || path.startsWith("\\")
                || path.contains("\\")
                || path.contains("\u0000")
                || path.contains(":")) {

            throw new IOException(
                    "Unsafe asset path: " + path
            );
        }

        String[] parts = path.split("/", -1);

        for (String part : parts) {

            if (part.isEmpty()
                    || part.equals(".")
                    || part.equals("..")) {

                throw new IOException(
                        "Unsafe asset path: " + path
                );
            }
        }
    }

    private static String createSafeDirectoryName(
            String packUid
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                    packUid.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte value : bytes) {

                result.append(
                        String.format(
                                Locale.ROOT,
                                "%02x",
                                value & 0xff
                        )
                );
            }

            return result.toString();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Cannot generate asset directory name",
                    exception
            );
        }
    }

    public static void deletePackAssets(
            Context context,
            String packUid
    ) {

        if (context == null
                || packUid == null
                || packUid.trim().isEmpty()) {

            return;
        }

        try {

            File root = new File(
                    context.getFilesDir(),
                    STORAGE_DIRECTORY
            );

            File canonicalRoot = root.getCanonicalFile();

            File directory = new File(
                    canonicalRoot,
                    createSafeDirectoryName(packUid)
            );

            if (!directory.getCanonicalPath().startsWith(
                    canonicalRoot.getCanonicalPath()
                            + File.separator
            )) {
                return;
            }

            deleteRecursively(directory);

        } catch (Exception ignored) {
            // Cleanup must not hide the original import error.
        }
    }

    public static void deleteRecursively(File file) {

        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {

            File[] children = file.listFiles();

            if (children != null) {

                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }

        file.delete();
    }
}
