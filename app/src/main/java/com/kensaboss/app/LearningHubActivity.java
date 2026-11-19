package com.kensaboss.app;

import android.content.*;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.kensaboss.app.localization.*;
import com.kensaboss.app.data.repository.*;
import com.kensaboss.app.models.*;
import com.kensaboss.app.importer.*;
import java.io.*;
import java.util.*;

/** Import, reference material and saved training history. */
public class LearningHubActivity extends AppCompatActivity {
    private LinearLayout content;
    private boolean importing;
    private final ActivityResultLauncher<String[]> picker = registerForActivityResult(
        new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null || importing) return;
            importing = true;
            render();
            Context context = getApplicationContext();
            new Thread(() -> {
                File temporary = null;
                String message;
                try {
                    temporary = File.createTempFile("import-", ".kbpack", context.getCacheDir());
                    try (InputStream in = context.getContentResolver().openInputStream(uri);
                         OutputStream out = new FileOutputStream(temporary)) {
                        if (in == null) throw new IOException("Cannot open file");
                        byte[] buffer = new byte[8192]; int count; long total = 0;
                        while ((count = in.read(buffer)) != -1) {
                            total += count;
                            if (total > 100L * 1024 * 1024) throw new IOException("Maximum package size: 100 MB");
                            out.write(buffer, 0, count);
                        }
                    }
                    PackImportResult result = ExplanationUpdateImporter.isJson(temporary) ? ExplanationUpdateImporter.importFile(context,temporary) : BookPackageInstaller.importFile(context, temporary);
                    message = result.isSuccessful() ? "Ներմուծումն ավարտված է / Импорт завершён / Imported.\n" + result.getImportedQuestions() + " հարց / вопросов / questions\n" + result.getImportedExplanations() + " բացատրություն / объяснений / explanations"
                        : android.text.TextUtils.join("\n", result.getErrors());
                } catch (Exception error) { message = String.valueOf(error.getMessage()); }
                finally { if (temporary != null) temporary.delete(); }
                final String resultMessage = message;
                runOnUiThread(() -> {
                    importing = false;
                    if (isFinishing() || isDestroyed()) return;
                    render();
                    new AlertDialog.Builder(this).setTitle(label("Добавление материалов", "Import materials", "Նյութերի ներմուծում"))
                        .setMessage(resultMessage).setPositiveButton(android.R.string.ok, null).show();
                });
            }, "content-import").start();
        });
    @Override protected void attachBaseContext(Context base) {
        super.attachBaseContext(FontSizeManager.wrap(LocaleManager.wrap(base)));
    }
    @Override protected void onCreate(Bundle state) {
        ThemeManager.applyTheme(this); super.onCreate(state); render();
    }
    private String label(String ru, String en, String hy) {
        String lang = getResources().getConfiguration().locale.getLanguage();
        return "en".equals(lang) ? en : "hy".equals(lang) ? hy : ru;
    }
    private void text(String value) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(17);
        view.setTextColor(getColor(R.color.kb_text_primary)); view.setPadding(12,18,12,18); content.addView(view);
    }
    private void button(String value, Runnable action) {
        Button button = new Button(this); button.setText(value); button.setAllCaps(false);
        content.addView(button); button.setOnClickListener(v -> action.run());
    }
    private void render() {
        ScrollView scroll = new ScrollView(this); content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL); content.setPadding(24,32,24,32);
        content.setBackgroundColor(getColor(R.color.kb_background)); scroll.addView(content); setContentView(scroll);
        button(label("Назад", "Back", "Հետ"), this::finish);
        String mode = getIntent().getStringExtra("mode");
        if ("import".equals(mode)) {
            text(label("Добавление учебных пакетов", "Import study packs", "Ուսումնական փաթեթների ներմուծում"));
            text(label("Выбери файл книги ZIP. Если книга уже добавлена, её разборы обновятся с сохранением результатов. PDF не преобразуется в вопросы автоматически.",
                "Choose the book ZIP. If the book is already present, its explanations are updated while your results are kept. PDFs are not converted automatically.",
                "Ընտրիր գրքի ZIP-ը։ Եթե գիրքն արդեն կա, բացատրությունները կթարմացվեն՝ պահպանելով արդյունքները։ PDF-ը ինքնաշխատ հարցեր չի դառնում։"));
            if (importing) text(label("Импорт выполняется…", "Importing…", "Ներմուծվում է…"));
            else button(label("Выбрать файл", "Choose file", "Ընտրել ֆայլ"), () -> picker.launch(new String[]{"*/*"}));
        } else if ("reference".equals(mode)) {
            text(label("Справочник: объяснения из учебных пакетов", "Reference: imported explanations", "Տեղեկատու․ ներմուծված բացատրություններ"));
            try (com.kensaboss.app.data.database.DatabaseHelper helper = new com.kensaboss.app.data.database.DatabaseHelper(this);
                 android.database.Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT MIN(q.id), ch.title, MIN(q.source_page) FROM questions q JOIN chapters ch ON ch.id=q.chapter_id GROUP BY ch.id ORDER BY ch.id",null)) {
                if(cursor.getCount()==0) text(label("Добавь учебный пакет.","Import a study pack.","Ներմուծիր ուսումնական փաթեթ։"));
                while(cursor.moveToNext()) {
                    final long questionId=cursor.getLong(0);final int page=cursor.getInt(2);
                    button(cursor.getString(1),()->{
                        if(StudyContent.resource(this,questionId,true)!=null) StudyContent.open(this,questionId,page,true);
                        else {Intent intent=new Intent(this,QuestionDetailsActivity.class);intent.putExtra(QuestionDetailsActivity.EXTRA_QUESTION_ID,questionId);startActivity(intent);}
                    });
                }
            }
        } else {
            text(label("История и результаты", "History and results", "Պատմություն և արդյունքներ"));
            try (TrainingSessionRepository repo = new TrainingSessionRepository(this)) {
                List<TrainingSession> sessions = repo.getAllSessions();
                if (sessions.isEmpty()) text(label("Начни первую тренировку.", "Start your first training session.", "Սկսիր առաջին վարժանքը։"));
                for (TrainingSession session : sessions) {
                    text("#" + session.getId() + " · " + session.getStartedAt() + "\n" + session.getStatus()
                        + "\n✓ " + session.getCorrectCount() + "   ✗ " + session.getIncorrectCount() + "   / " + session.getTotalQuestions());
                    button(label("Открыть", "Open", "Բացել"), () -> {
                        if (TrainingSession.STATUS_IN_PROGRESS.equals(session.getStatus())) {
                            Intent intent = new Intent(this, TrainingQuestionActivity.class);
                            intent.putExtra(TrainingQuestionActivity.EXTRA_SESSION_ID, session.getId()); startActivity(intent);
                        } else showReview(session.getId());
                    });
                }
            }
        }
    }
    private void showReview(long sessionId) {
        content.removeAllViews(); button(label("Назад", "Back", "Հետ"), this::render);
        try (TrainingSessionRepository sessions = new TrainingSessionRepository(this);
             SessionAnswerRepository answers = new SessionAnswerRepository(this);
             QuestionRepository questions = new QuestionRepository(this);
             AnswerOptionRepository options = new AnswerOptionRepository(this);
             CorrectAnswerRepository keys = new CorrectAnswerRepository(this)) {
            for (Long id : sessions.getQuestionIdsForSession(sessionId)) {
                Question question = questions.getQuestionById(id); if (question == null) continue;
                SessionAnswer answer = answers.getAnswerByAttempt(sessionId,id,1);
                text(question.getQuestionText());
                text(answer == null ? label("Нет ответа", "Unanswered", "Պատասխան չկա") :
                    (answer.getIsCorrect() == null ? "?" : answer.getIsCorrect() ? "✓" : "✗") + " " + displayAnswer(answer.getSelectedAnswer(), options.getAnswerOptionsByQuestionId(id)));
                StringBuilder correct = new StringBuilder();
                for (CorrectAnswer key : keys.getCorrectAnswersByQuestionId(id)) {
                    if (!CorrectAnswer.STATUS_VERIFIED.equals(key.getVerificationStatus())) continue;
                    if (key.getOptionId() != null) correct.append(displayAnswer(String.valueOf(key.getOptionId()),options.getAnswerOptionsByQuestionId(id)));
                    else correct.append(key.getAnswerValue());
                    correct.append("\n");
                }
                text(label("Правильный ответ: ", "Correct answer: ", "Ճիշտ պատասխան․ ") + correct);
                button(label("Объяснение", "Explanation", "Բացատրություն"), () -> {
                    Intent intent = new Intent(this, QuestionDetailsActivity.class);
                    intent.putExtra(QuestionDetailsActivity.EXTRA_QUESTION_ID,id); startActivity(intent);
                });
            }
        }
    }
    private String displayAnswer(String answer, List<AnswerOption> options) {
        if (answer == null) return "";
        List<String> selected = Arrays.asList(answer.split(",")); StringBuilder result = new StringBuilder();
        for (AnswerOption option : options) if (selected.contains(String.valueOf(option.getId()))) result.append(option.getOptionText()).append("; ");
        return result.length() == 0 ? answer : result.toString();
    }
    private String safe(String value) { return value == null ? "" : value; }
}