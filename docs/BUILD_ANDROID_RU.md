# Сборка KensaBoss в Android Studio (Windows)

## Требования

- Android Studio с Android SDK Platform 36.1.
- JDK 21. Gradle wrapper скачает Gradle 9.5.0 и Gradle Plugin из объявленных в проекте репозиториев.
- Windows 10/11 и Android-телефон с Android 6.0 или новее либо эмулятор.

## Открыть и запустить

1. Скачайте исходный ZIP репозитория и распакуйте его в обычную папку, например `C:\Projects\KensaBoss`.
2. В Android Studio выберите **Open** и укажите распакованную папку, где находятся `settings.gradle.kts` и `gradlew.bat`.
3. Дождитесь Gradle Sync. Если Android Studio попросит SDK Platform 36.1, установите её через SDK Manager.
4. Подключите телефон с включённой отладкой USB или создайте эмулятор Android 6.0+.
5. Выберите конфигурацию `app` и нажмите **Run**.

После установки откройте «Учебники» → «Добавить учебный пакет» и выберите подготовленный ZIP-файл. Тогда книга появится в библиотеке.

## Собрать APK для локальной установки

Из корня проекта можно выполнить:

```powershell
.\gradlew.bat assembleDebug
```

Готовый файл появится здесь:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Передавайте debug APK только для тестирования. Для публичного распространения через Google Play нужно настроить отдельный release-вариант и подписать его собственным ключом. Не загружайте приватный ключ подписи в GitHub.

## Где находятся основные части

- `MainActivity`, `LearningHubActivity`, `BooksActivity` — основные экраны и импорт пакетов.
- `TrainingQuestionActivity` — прохождение заданий и показ ответов.
- `data/database` и `data/repository` — локальная база SQLite и доступ к данным.
- `importer` — чтение и проверка ZIP-пакетов, импорт книг и обновление объяснений.
- `localization` и `res/values*` — языки интерфейса, тема и настройки текста.
