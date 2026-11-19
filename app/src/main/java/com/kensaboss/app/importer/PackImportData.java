
package com.kensaboss.app.importer;

import org.json.JSONArray;

public class PackImportData {

    private final PackManifest manifest;

    private final JSONArray books;
    private final JSONArray bookParts;
    private final JSONArray chapters;
    private final JSONArray sections;
    private final JSONArray questions;

    private final JSONArray answerOptions;
    private final JSONArray correctAnswers;
    private final JSONArray explanations;
    private final JSONArray explanationAssets;

    public PackImportData(
            PackManifest manifest,
            JSONArray books,
            JSONArray bookParts,
            JSONArray chapters,
            JSONArray sections,
            JSONArray questions
    ) {
        this(
                manifest,
                books,
                bookParts,
                chapters,
                sections,
                questions,
                new JSONArray(),
                new JSONArray(),
                new JSONArray(),
                new JSONArray()
        );
    }

    public PackImportData(
            PackManifest manifest,
            JSONArray books,
            JSONArray bookParts,
            JSONArray chapters,
            JSONArray sections,
            JSONArray questions,
            JSONArray answerOptions,
            JSONArray correctAnswers,
            JSONArray explanations,
            JSONArray explanationAssets
    ) {
        if (manifest == null) {
            throw new IllegalArgumentException(
                    "Manifest must not be null"
            );
        }

        this.manifest = manifest;

        this.books = copyArray(books);
        this.bookParts = copyArray(bookParts);
        this.chapters = copyArray(chapters);
        this.sections = copyArray(sections);
        this.questions = copyArray(questions);

        this.answerOptions = copyArray(answerOptions);
        this.correctAnswers = copyArray(correctAnswers);
        this.explanations = copyArray(explanations);
        this.explanationAssets = copyArray(explanationAssets);
    }

    private static JSONArray copyArray(JSONArray source) {
        if (source == null) {
            return new JSONArray();
        }

        JSONArray copy = new JSONArray();

        for (int i = 0; i < source.length(); i++) {
            copy.put(source.opt(i));
        }

        return copy;
    }

    public PackManifest getManifest() {
        return manifest;
    }

    public JSONArray getBooks() {
        return copyArray(books);
    }

    public JSONArray getBookParts() {
        return copyArray(bookParts);
    }

    public JSONArray getChapters() {
        return copyArray(chapters);
    }

    public JSONArray getSections() {
        return copyArray(sections);
    }

    public JSONArray getQuestions() {
        return copyArray(questions);
    }

    public JSONArray getAnswerOptions() {
        return copyArray(answerOptions);
    }

    public JSONArray getCorrectAnswers() {
        return copyArray(correctAnswers);
    }

    public JSONArray getExplanations() {
        return copyArray(explanations);
    }

    public JSONArray getExplanationAssets() {
        return copyArray(explanationAssets);
    }

    // Количество книг.
    public int getBooksCount() {
        return books.length();
    }

    public int getBookCount() {
        return getBooksCount();
    }

    // Количество частей книг.
    public int getBookPartsCount() {
        return bookParts.length();
    }

    public int getBookPartCount() {
        return getBookPartsCount();
    }

    // Количество глав.
    public int getChaptersCount() {
        return chapters.length();
    }

    public int getChapterCount() {
        return getChaptersCount();
    }

    // Количество разделов.
    public int getSectionsCount() {
        return sections.length();
    }

    public int getSectionCount() {
        return getSectionsCount();
    }

    // Количество вопросов.
    public int getQuestionsCount() {
        return questions.length();
    }

    public int getQuestionCount() {
        return getQuestionsCount();
    }

    // Количество вариантов ответов.
    public int getAnswerOptionsCount() {
        return answerOptions.length();
    }

    public int getAnswerOptionCount() {
        return getAnswerOptionsCount();
    }

    // Количество правильных ответов.
    public int getCorrectAnswersCount() {
        return correctAnswers.length();
    }

    public int getCorrectAnswerCount() {
        return getCorrectAnswersCount();
    }

    // Количество объяснений.
    public int getExplanationsCount() {
        return explanations.length();
    }

    public int getExplanationCount() {
        return getExplanationsCount();
    }

    // Количество файлов и изображений объяснений.
    public int getExplanationAssetsCount() {
        return explanationAssets.length();
    }

    public int getExplanationAssetCount() {
        return getExplanationAssetsCount();
    }

    public int getTotalCount() {
        return getBooksCount()
                + getBookPartsCount()
                + getChaptersCount()
                + getSectionsCount()
                + getQuestionsCount()
                + getAnswerOptionsCount()
                + getCorrectAnswersCount()
                + getExplanationsCount()
                + getExplanationAssetsCount();
    }
}
