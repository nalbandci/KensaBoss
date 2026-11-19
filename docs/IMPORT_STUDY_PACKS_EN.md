# Preparing and importing a study pack

KensaBoss imports complete study packs as ZIP archives. Select **Books → Add study pack** in the app, then choose the archive. Keep the package structure at the root of the archive; do not put the files inside an extra wrapper folder.

## Required structure

The archive must contain a root-level **manifest.json** and JSON files under **data/**:

- **data/books.json**
- **data/book_parts.json**
- **data/chapters.json**
- **data/sections.json**
- **data/questions.json**
- **data/answer_options.json**
- **data/correct_answers.json**

The importer can also read:

- **data/explanations.json**
- **data/explanation_assets.json**

The manifest identifies the pack. Give each separate book or edition a stable, unique **packUid**. Preserve each record's **externalId** and all references between books, chapters, questions, answer options, and correct answers.

Supported question types are **SINGLE_CHOICE**, **MULTIPLE_CHOICE**, **NUMERIC**, **TEXT**, **MATCHING**, and **SEQUENCE**. Choice answers refer to an option using **optionExternalId**; numeric and text answers use **answerValue**.

## Images, diagrams, and other lesson assets

If the pack contains explanation assets, **explanation_assets.json** refers to files stored under the archive's **assets/** directory. Keep each **filePath** aligned with the actual path in the archive. Include only material that you have permission to distribute.

## Updating explanations

An explanation refresh for an installed book is placed at **updates/explanations.json** in the ZIP. The update must target the same **packUid** and matching edition. KensaBoss checks the question fingerprints and explanation revisions before applying it; a valid update changes explanations and preserves study progress.

Do not use an explanation update to change questions, answer keys, book structure, or edition. Prepare a new complete pack for those changes.

## PDF and content review

KensaBoss does not parse raw PDFs or generate quizzes with AI. Convert and review the source material before building a pack. The app checks data structure and references; it does not fact-check the subject content. Have a qualified teacher review educational content and answer keys.

Keep the application source separate from textbooks, question banks, answer keys, and scanned PDFs. Share these materials only when you have the right to do so.