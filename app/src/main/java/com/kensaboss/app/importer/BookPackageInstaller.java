package com.kensaboss.app.importer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.kensaboss.app.data.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** One book file for new installs and for explanation-only refreshes of the same edition. */
public final class BookPackageInstaller {
    private BookPackageInstaller() { }
    public static PackImportResult importFile(Context context,File archive) {
        try {
            PackImportData data=PackImportDataLoader.load(archive);
            PackImportResult validation=PackImportValidator.validate(data);
            if(!validation.isSuccessful())return validation;
            String uid=data.getManifest().getPackUid();
            boolean exists;
            try(DatabaseHelper helper=new DatabaseHelper(context.getApplicationContext());
                Cursor c=helper.getReadableDatabase().rawQuery("SELECT id FROM content_packs WHERE pack_uid=?",new String[]{uid})) {
                exists=c.moveToFirst();
            }
            if(!exists)return PackImporter.importPack(context,archive,data);
            JSONObject update=readUpdate(archive);
            if(!uid.equals(update.getString("targetPackUid")))throw new IllegalArgumentException("Book and explanations refer to different packs");
            verifyUpdateMatchesBook(data,update);
            // Edition validation and all explanation changes share the same transaction.
            return ExplanationUpdateImporter.importDocument(context,update,db->verifyInstalledEdition(db,data));
        }catch(Exception error) {
            PackImportResult result=new PackImportResult();
            result.addError(error.getMessage()==null?error.getClass().getSimpleName():error.getMessage());
            result.setStatus(PackImportResult.STATUS_FAILED);return result;
        }
    }
    private static JSONObject readUpdate(File archive)throws Exception {
        try(ZipFile zip=new ZipFile(archive)) {
            ZipEntry entry=zip.getEntry("updates/explanations.json");
            if(entry==null)throw new IllegalArgumentException("This book is already installed. Its file does not contain an explanation refresh.");
            if(entry.getSize()>10L*1024*1024)throw new IOException("Book explanation refresh exceeds 10 MB");
            try(InputStream in=zip.getInputStream(entry);ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[8192];int count;
                while((count=in.read(buffer))!=-1){if(out.size()+count>10*1024*1024)throw new IOException("Book explanation refresh exceeds 10 MB");out.write(buffer,0,count);}
                return new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
            }
        }
    }
    private static void verifyUpdateMatchesBook(PackImportData data,JSONObject update)throws Exception {
        Map<String,JSONObject> questions=index(data.getQuestions(),"externalId");
        Map<String,JSONObject> explanations=new HashMap<>();
        JSONArray source=data.getExplanations();
        for(int i=0;i<source.length();i++) {
            JSONObject e=source.getJSONObject(i);String key=e.getString("questionExternalId")+":"+e.getString("language");
            if(explanations.put(key,e)!=null)throw new IllegalArgumentException("Duplicate book explanation: "+key);
        }
        JSONArray entries=update.getJSONArray("explanations");Set<String> seen=new HashSet<>();
        for(int i=0;i<entries.length();i++) {
            JSONObject u=entries.getJSONObject(i);String id=u.getString("questionExternalId"),key=id+":"+u.getString("language");
            JSONObject q=questions.get(id),e=explanations.get(key);
            if(!seen.add(key)||q==null||e==null)throw new IllegalArgumentException("Unmatched book explanation: "+key);
            if(!ExplanationUpdateImporter.questionHash(id,q.getString("questionText"),q.getString("questionType")).equals(u.getString("questionHash")))throw new IllegalArgumentException("Question fingerprint mismatch: "+id);
            if(e.getInt("version")!=u.getInt("revision"))throw new IllegalArgumentException("Book explanation version mismatch: "+id);
            for(String field:new String[]{"introduction","detailedText","solutionSteps","additionalNotes"})
                if(!e.optString(field,"").equals(u.optString(field,"")))throw new IllegalArgumentException("Book explanation differs from embedded refresh: "+id);
            String status=q.optString("sourceKeyStatus","");
            if(!"VERIFIED".equals(status)&&!status.equals(u.optString("sourceKeyStatus","")))throw new IllegalArgumentException("Book review status mismatch: "+id);
            if(u.has("sourceKeyStatus")&&!status.equals(u.getString("sourceKeyStatus")))throw new IllegalArgumentException("Book review status mismatch: "+id);
        }
        for(Map.Entry<String,JSONObject> e:explanations.entrySet())
            if(e.getValue().optInt("version",1)>=2&&!seen.contains(e.getKey()))throw new IllegalArgumentException("Embedded refresh omits book explanation: "+e.getKey());
    }
    private static Map<String,JSONObject> index(JSONArray entries,String field)throws Exception {
        Map<String,JSONObject> map=new HashMap<>();
        for(int i=0;i<entries.length();i++){JSONObject e=entries.getJSONObject(i);String key=e.getString(field);if(map.put(key,e)!=null)throw new IllegalArgumentException("Ambiguous book identifier: "+key);}
        return map;
    }
    private static void verifyInstalledEdition(SQLiteDatabase db,PackImportData data)throws Exception {
        String uid=data.getManifest().getPackUid();
        Map<String,JSONObject> questions=index(data.getQuestions(),"externalId");
        Map<String,JSONObject> options=index(data.getAnswerOptions(),"externalId");
        Set<String> seenQuestions=new HashSet<>(),seenOptions=new HashSet<>();
        try(Cursor c=db.rawQuery("SELECT q.external_id,q.question_text,q.question_type,b.external_id,ch.external_id,COALESCE(s.external_id,''),COALESCE(q.original_number,''),COALESCE(q.source_page,0),q.sort_order FROM questions q JOIN books b ON b.id=q.book_id JOIN content_packs p ON p.id=b.pack_id JOIN chapters ch ON ch.id=q.chapter_id LEFT JOIN sections s ON s.id=q.section_id WHERE p.pack_uid=?",new String[]{uid})) {
            while(c.moveToNext()) {
                String id=c.getString(0);JSONObject q=questions.get(id);
                if(q==null||!seenQuestions.add(id))throw new IllegalArgumentException("Different book question set: "+id);
                String[] fields={"questionText","questionType","bookExternalId","chapterExternalId","sectionExternalId","originalNumber"};
                for(int i=0;i<fields.length;i++)if(!c.getString(i+1).equals(q.optString(fields[i],"")))throw new IllegalArgumentException("Installed book edition differs: "+id);
                if(c.getInt(7)!=q.optInt("sourcePage",0)||c.getInt(8)!=q.optInt("sortOrder",0))throw new IllegalArgumentException("Installed question location differs: "+id);
            }
        }
        if(seenQuestions.size()!=questions.size())throw new IllegalArgumentException("Installed book has a different question count");
        try(Cursor c=db.rawQuery("SELECT o.external_id,q.external_id,o.option_text,COALESCE(o.option_label,''),o.sort_order FROM answer_options o JOIN questions q ON q.id=o.question_id JOIN books b ON b.id=q.book_id JOIN content_packs p ON p.id=b.pack_id WHERE p.pack_uid=?",new String[]{uid})) {
            while(c.moveToNext()) {
                String id=c.getString(0);JSONObject o=options.get(id);
                if(o==null||!seenOptions.add(id)||!c.getString(1).equals(o.getString("questionExternalId"))||!c.getString(2).equals(o.getString("optionText"))||!c.getString(3).equals(o.optString("optionLabel",""))||c.getInt(4)!=o.optInt("sortOrder",0))throw new IllegalArgumentException("Installed answer options differ: "+id);
            }
        }
        if(seenOptions.size()!=options.size())throw new IllegalArgumentException("Installed book has a different option count");
        List<String> expected=new ArrayList<>(),installed=new ArrayList<>();JSONArray answers=data.getCorrectAnswers();
        for(int i=0;i<answers.length();i++){JSONObject a=answers.getJSONObject(i);expected.add(new JSONArray().put(a.getString("questionExternalId")).put(a.optString("optionExternalId","")).put(a.optString("answerValue","")).toString());}
        try(Cursor c=db.rawQuery("SELECT q.external_id,COALESCE(o.external_id,''),COALESCE(a.answer_value,'') FROM correct_answers a JOIN questions q ON q.id=a.question_id JOIN books b ON b.id=q.book_id JOIN content_packs p ON p.id=b.pack_id LEFT JOIN answer_options o ON o.id=a.option_id WHERE p.pack_uid=?",new String[]{uid})) {
            while(c.moveToNext())installed.add(new JSONArray().put(c.getString(0)).put(c.getString(1)).put(c.getString(2)).toString());
        }
        Collections.sort(expected);Collections.sort(installed);
        if(!expected.equals(installed))throw new IllegalArgumentException("Installed printed answer keys differ; book was not changed");
    }
}
