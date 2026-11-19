package com.kensaboss.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kensaboss.app.data.database.DatabaseHelper;
import java.io.File;

/** Resolves source documents within the question's own content pack. */
public final class StudyContent {
    private StudyContent() { }
    public static String resource(Activity activity, long questionId, boolean lesson) {
        try (DatabaseHelper helper = new DatabaseHelper(activity)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            String sql = "SELECT a.file_path FROM questions q JOIN books b ON b.id=q.book_id "
                + "JOIN chapters ch ON ch.id=q.chapter_id JOIN books sb ON sb.pack_id=b.pack_id "
                + "JOIN questions sq ON sq.book_id=sb.id JOIN explanations e ON e.question_id=sq.id "
                + "JOIN explanation_assets a ON a.explanation_id=e.id WHERE q.id=? AND "
                + (lesson ? "a.description=('@lesson:' || ch.external_id)" : "a.description='@source-document'") + " LIMIT 1";
            try (Cursor c = db.rawQuery(sql,new String[]{String.valueOf(questionId)})) {
                if (c.moveToFirst()) {
                    String stored = c.getString(0);
                    File candidate = new File(stored);
                    File file = (candidate.isAbsolute() ? candidate : new File(activity.getFilesDir(),stored)).getCanonicalFile();
                    File root = new File(activity.getFilesDir(),"content_packs").getCanonicalFile();
                    if (file.isFile() && file.getPath().startsWith(root.getPath()+File.separator)) return file.getPath();
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
    public static void open(Activity activity,long questionId,int page, boolean lesson) {
        Intent intent = new Intent(activity,SourcePagesActivity.class);
        intent.putExtra("question_id",questionId);intent.putExtra("page",page);intent.putExtra("lesson",lesson);
        activity.startActivity(intent);
    }
    public static void tools(Activity activity, LinearLayout container,long questionId,int page) {
        if(resource(activity,questionId,false)!=null) {
            Button source=new Button(activity);source.setAllCaps(false);source.setText("Բնօրինակ PDF · էջ "+page);
            source.setTag("study-tool");source.setOnClickListener(v->open(activity,questionId,page,false));container.addView(source);
        }
        if(resource(activity,questionId,true)!=null) {
            Button lesson=new Button(activity);lesson.setAllCaps(false);lesson.setText("Թեմայի բացատրություն և սխեմաներ");
            lesson.setTag("study-tool");lesson.setOnClickListener(v->open(activity,questionId,page,true));container.addView(lesson);
        }
    }
    public static void explanation(Activity activity, LinearLayout container, long questionId) {
        try (com.kensaboss.app.data.repository.ExplanationRepository repo = new com.kensaboss.app.data.repository.ExplanationRepository(activity)) {
            for(com.kensaboss.app.models.Explanation e: repo.getExplanationsByQuestionId(questionId)) {
                TextView view=new TextView(activity);view.setTextSize(17);view.setTextColor(activity.getColor(R.color.kb_text_primary));
                String notes=safe(e.getAdditionalNotes());
                java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\[PDF_KEY_PAGE:(\\d+)\\]").matcher(notes);
                if(m.find()) {
                    int keyPage=Integer.parseInt(m.group(1));Button key=new Button(activity);key.setAllCaps(false);
                    key.setText("Տպագիր պատասխանների էջը · "+keyPage); key.setOnClickListener(v->open(activity,questionId,keyPage,false));container.addView(key);
                }
                view.setText(safe(e.getIntroduction())+"\n\n"+safe(e.getDetailedText())+"\n\n"+safe(e.getSolutionSteps())+"\n\n"+notes.replaceAll("\\[PDF_KEY_PAGE:\\d+\\]", ""));
                view.setPadding(12,20,12,20);container.addView(view);
            }
        }
    }
    public static void showExplanation(Activity activity,long questionId) {
        android.widget.ScrollView scroll=new android.widget.ScrollView(activity);
        LinearLayout body=new LinearLayout(activity);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(20,16,20,16);scroll.addView(body);
        explanation(activity,body,questionId);
        new androidx.appcompat.app.AlertDialog.Builder(activity).setTitle("Բացատրություն և աղբյուր").setView(scroll).setPositiveButton(android.R.string.ok,null).show();
    }
    private static String safe(String text){return text==null?"":text;}
}
