package com.kensaboss.app.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackImportResult {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PARTIAL = "PARTIAL";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String status;

    private int importedBooks;
    private int importedBookParts;
    private int importedChapters;
    private int importedSections;
    private int importedQuestions;

    private int importedAnswerOptions;
    private int importedCorrectAnswers;
    private int importedExplanations;
    private int importedExplanationAssets;

    private final List<String> errors;

    public PackImportResult() {
        status = STATUS_FAILED;
        errors = new ArrayList<>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Import status cannot be empty"
            );
        }

        if (!STATUS_SUCCESS.equals(status)
                && !STATUS_FAILED.equals(status)
                && !STATUS_PARTIAL.equals(status)
                && !STATUS_CANCELLED.equals(status)) {

            throw new IllegalArgumentException(
                    "Unknown import status: " + status
            );
        }

        this.status = status;
    }

    public int getImportedBooks() {
        return importedBooks;
    }

    public void setImportedBooks(int count) {
        importedBooks = validateCount(count);
    }

    public int getImportedBookParts() {
        return importedBookParts;
    }

    public void setImportedBookParts(int count) {
        importedBookParts = validateCount(count);
    }

    public int getImportedChapters() {
        return importedChapters;
    }

    public void setImportedChapters(int count) {
        importedChapters = validateCount(count);
    }

    public int getImportedSections() {
        return importedSections;
    }

    public void setImportedSections(int count) {
        importedSections = validateCount(count);
    }

    public int getImportedQuestions() {
        return importedQuestions;
    }

    public void setImportedQuestions(int count) {
        importedQuestions = validateCount(count);
    }

    public int getImportedAnswerOptions() {
        return importedAnswerOptions;
    }

    public void setImportedAnswerOptions(int count) {
        importedAnswerOptions = validateCount(count);
    }

    public int getImportedCorrectAnswers() {
        return importedCorrectAnswers;
    }

    public void setImportedCorrectAnswers(int count) {
        importedCorrectAnswers = validateCount(count);
    }

    public int getImportedExplanations() {
        return importedExplanations;
    }

    public void setImportedExplanations(int count) {
        importedExplanations = validateCount(count);
    }

    public int getImportedExplanationAssets() {
        return importedExplanationAssets;
    }

    public void setImportedExplanationAssets(int count) {
        importedExplanationAssets = validateCount(count);
    }

    public void addError(String error) {

        if (error == null || error.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Error message cannot be empty"
            );
        }

        errors.add(error.trim());
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public int getTotalImported() {

        return importedBooks
                + importedBookParts
                + importedChapters
                + importedSections
                + importedQuestions
                + importedAnswerOptions
                + importedCorrectAnswers
                + importedExplanations
                + importedExplanationAssets;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public boolean isSuccessful() {
        return STATUS_SUCCESS.equals(status);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private int validateCount(int count) {

        if (count < 0) {
            throw new IllegalArgumentException(
                    "Imported count cannot be negative"
            );
        }

        return count;
    }
}