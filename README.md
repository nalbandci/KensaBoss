# KensaBoss

**An offline-first Android learning app with interactive, chapter-based practice.**

[English](#english) · [Русский](#русский)

## English

### Why I created it

I created KensaBoss for my younger sister, who is studying biology in Armenian and preparing for university admission. I wanted to make her study routine easier to follow: choose a topic, practise questions, review explanations, and keep track of progress.

KensaBoss started as a biology learning app. Its library and import flow are designed to work with prepared study packs for different books and subjects.

### What you can do

- Use the interface in Armenian, Russian, or English.
- Import a complete prepared ZIP study pack and browse it by book, part, chapter, section, and question.
- Practise single-choice, multiple-choice, numeric, text, matching, and sequence questions.
- Review answer explanations, solution steps, diagrams, and other learning materials provided with a pack.
- Save progress locally, view study history, and resume an unfinished session.
- Choose light, dark, or system theme and adjust text size.
- Update explanations for a matching edition while keeping study progress.

### How to use the app

1. Open the app and go to **Books**.
2. Select **Add study pack** and choose a prepared ZIP file. A single package can contain a complete book with all its chapters.
3. Open a book, choose a part or chapter, and start a practice session.
4. Review the answer and explanation after each question. Your progress is saved on the device.

KensaBoss imports prepared ZIP packs. It does not currently convert raw PDF files into quizzes or generate questions with AI. See the [English guide to study packs](docs/IMPORT_STUDY_PACKS_EN.md) or [Russian guide](docs/IMPORT_STUDY_PACKS_RU.md).

### Build

Requirements: Android Studio, JDK 21, and Android SDK Platform 36.1. The app supports Android 6.0 (API 23) and newer.

From the project root on Windows:

    ./gradlew.bat assembleDebug

On macOS or Linux:

    ./gradlew assembleDebug

The debug APK is created at **app/build/outputs/apk/debug/app-debug.apk**. For step-by-step Windows instructions, see [Building in Android Studio](docs/BUILD_ANDROID_RU.md).

### Technology

Java, AndroidX, Material Components, Gradle, and SQLite. The app works locally and does not require an account or server.

### License

The application source code is shared under the [MIT License](LICENSE).

## Русский

### Для чего я создал приложение

Я создал KensaBoss для младшей сестры: она изучает биологию на армянском языке и готовится к поступлению в университет. Мне хотелось сделать занятия последовательными и удобными: выбирать тему, решать задания, разбирать ответы и видеть свой прогресс.

KensaBoss начинался как приложение для изучения биологии. Библиотека и импорт пакетов позволяют добавлять подготовленные материалы по разным книгам и предметам.

### Возможности

- Интерфейс на армянском, русском и английском языках.
- Импорт целого подготовленного ZIP-пакета и просмотр материала по книгам, частям, главам, разделам и заданиям.
- Тренировки с выбором одного или нескольких ответов, числовым и текстовым ответом, сопоставлением и последовательностью.
- Просмотр объяснений, шагов решения, схем и других материалов, включённых в пакет.
- Локальное сохранение прогресса, история занятий и продолжение незавершённой тренировки.
- Светлая, тёмная и системная темы, настройка размера текста.
- Обновление объяснений для совпадающего издания с сохранением учебного прогресса.

### Как пользоваться

1. Откройте приложение и перейдите в раздел **«Учебники»**.
2. Нажмите **«Добавить учебный пакет»** и выберите подготовленный ZIP-файл. В одном пакете можно разместить книгу целиком вместе со всеми главами.
3. Откройте книгу, выберите часть или главу и начните тренировку.
4. После задания смотрите ответ и объяснение. Прогресс сохраняется на устройстве.

KensaBoss импортирует заранее подготовленные ZIP-пакеты. Приложение пока не преобразует исходные PDF-файлы в тесты и не создаёт вопросы с помощью ИИ. См. [инструкцию по учебным пакетам на русском](docs/IMPORT_STUDY_PACKS_RU.md) или [на английском](docs/IMPORT_STUDY_PACKS_EN.md).

### Сборка

Понадобятся Android Studio, JDK 21 и Android SDK Platform 36.1. Приложение поддерживает Android 6.0 (API 23) и новее.

В корне проекта выполните:

    ./gradlew.bat assembleDebug

Пошаговая инструкция для Windows находится в разделе [сборки Android Studio](docs/BUILD_ANDROID_RU.md). Debug APK появится в **app/build/outputs/apk/debug/app-debug.apk**.

### Технологии и лицензия

Приложение написано на Java с использованием AndroidX, Material Components, Gradle и SQLite. Оно работает локально, без учётной записи и сервера. Исходный код распространяется по лицензии [MIT](LICENSE).