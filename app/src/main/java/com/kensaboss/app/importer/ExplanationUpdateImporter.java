package com.kensaboss.app.importer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.kensaboss.app.data.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Atomic, edition-checked updates of explanations; questions, keys and progress stay intact. */
public final class ExplanationUpdateImporter {
    public static final String FORMAT="KensaBoss.explanations.v1";
    private ExplanationUpdateImporter() { }
    public static boolean isJson(File file)throws IOException {
        try(InputStream in=new FileInputStream(file)) {
            int c;do{c=in.read();}while(c>=0&&Character.isWhitespace(c));
            return c=='{' || c==0xEF;
        }
    }
    public static PackImportResult importFile(Context context,File file) {
        PackImportResult result=new PackImportResult();
        try {
            if(file.length()>10L*1024*1024)throw new IOException("Explanation update exceeds 10 MB");
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            try(InputStream in=new FileInputStream(file)) {
                byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(output.size()+n>10*1024*1024)throw new IOException("Update too large");output.write(buffer,0,n);}
            }
            String text=new String(output.toByteArray(),StandardCharsets.UTF_8);
            if(text.startsWith("\uFEFF"))text=text.substring(1);
            JSONObject document=new JSONObject(text);
            return importDocument(context,document,null);
        }catch(Exception error){result.addError(error.getMessage()==null?error.getClass().getSimpleName():error.getMessage());result.setStatus(PackImportResult.STATUS_FAILED);}
        return result;
    }
    interface EditionCheck { void verify(SQLiteDatabase database)throws Exception; }
    static PackImportResult importDocument(Context context,JSONObject document,EditionCheck editionCheck) {
        PackImportResult result=new PackImportResult();
        try {
            if(!FORMAT.equals(document.optString("format")))throw new IllegalArgumentException("Unsupported explanation update format");
            String packUid=required(document,"targetPackUid");
            JSONArray entries=document.getJSONArray("explanations");
            if(entries.length()==0||entries.length()>10000)throw new IllegalArgumentException("Invalid explanation count");
            try(DatabaseHelper helper=new DatabaseHelper(context.getApplicationContext())) {
                SQLiteDatabase db=helper.getWritableDatabase();
                db.beginTransaction();
                try {
                    if(editionCheck!=null)editionCheck.verify(db);
                    long packId;
                    try(Cursor c=db.rawQuery("SELECT id FROM content_packs WHERE pack_uid=?",new String[]{packUid})) {
                        if(!c.moveToFirst())throw new IllegalArgumentException("Import the matching book before its explanation update");packId=c.getLong(0);
                    }
                    Set<String> seen=new HashSet<>();List<Pending> pending=new ArrayList<>();int unchanged=0;
                    for(int i=0;i<entries.length();i++) {
                        JSONObject e=entries.getJSONObject(i);
                        String external=required(e,"questionExternalId"),language=required(e,"language"),hash=required(e,"questionHash");
                        if(!language.matches("[a-z]{2,3}")||!hash.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Invalid language or question hash: "+external);
                        if(!seen.add(external+":"+language))throw new IllegalArgumentException("Duplicate explanation: "+external);
                        long questionId;
                        try(Cursor c=db.rawQuery("SELECT q.id,q.question_text,q.question_type FROM questions q JOIN books b ON b.id=q.book_id WHERE b.pack_id=? AND q.external_id=?",new String[]{String.valueOf(packId),external})) {
                            if(c.getCount()!=1||!c.moveToFirst())throw new IllegalArgumentException("Missing or ambiguous question: "+external);
                            questionId=c.getLong(0);
                            if(!hash.equals(questionHash(external,c.getString(1),c.getString(2))))throw new IllegalArgumentException("Book edition does not match: "+external);
                        }
                        Object rev=e.get("revision");
                        if(!(rev instanceof Integer||rev instanceof Long)||((Number)rev).longValue()<2||((Number)rev).longValue()>Integer.MAX_VALUE)throw new IllegalArgumentException("Invalid revision");
                        int revision=((Number)rev).intValue();
                        String reviewStatus=e.optString("sourceKeyStatus","");
                        if(!reviewStatus.isEmpty()&&!Arrays.asList("CONFLICTING","AMBIGUOUS","PRINTING_ERROR").contains(reviewStatus))throw new IllegalArgumentException("Invalid review status");
                        ContentValues values=new ContentValues();values.put("question_id",questionId);values.put("language",language);values.put("version",revision);
                        values.put("introduction",required(e,"introduction"));values.put("detailed_text",required(e,"detailedText"));
                        values.put("solution_steps",e.optString("solutionSteps",""));
                        String oldNotes="";long existingId=-1;int oldVersion=0;String[] oldText=null;
                        try(Cursor c=db.rawQuery("SELECT id,version,additional_notes,introduction,detailed_text,solution_steps FROM explanations WHERE question_id=? AND language=?",new String[]{String.valueOf(questionId),language})) {
                            if(c.moveToFirst()){existingId=c.getLong(0);oldVersion=c.getInt(1);oldNotes=c.isNull(2)?"":c.getString(2);oldText=new String[]{c.isNull(3)?"":c.getString(3),c.isNull(4)?"":c.getString(4),c.isNull(5)?"":c.getString(5)};}
                        }
                        String notes=e.optString("additionalNotes","");
                        java.util.regex.Matcher marker=java.util.regex.Pattern.compile("\\[PDF_KEY_PAGE:\\d+\\]").matcher(oldNotes);
                        if(marker.find()&&!notes.contains(marker.group()))notes=marker.group()+"\n"+notes;
                        values.put("additional_notes",notes);
                        for(String field:new String[]{"introduction","detailed_text","solution_steps","additional_notes"})if(values.getAsString(field).length()>64000)throw new IllegalArgumentException("Explanation too long: "+external);
                        if(revision<oldVersion)throw new IllegalArgumentException("Update would downgrade: "+external);
                        if(revision==oldVersion){
                            if(!oldText[0].equals(values.getAsString("introduction"))||!oldText[1].equals(values.getAsString("detailed_text"))||!oldText[2].equals(values.getAsString("solution_steps"))||!oldNotes.equals(notes))throw new IllegalArgumentException("Revision already exists with different content: "+external);
                            unchanged++;continue;
                        }
                        pending.add(new Pending(existingId,values,questionId,reviewStatus));
                    }
                    for(Pending p:pending) {
                        if(p.id<0)db.insertOrThrow("explanations",null,p.values);
                        else if(db.update("explanations",p.values,"id=?",new String[]{String.valueOf(p.id)})!=1)throw new IllegalStateException("Explanation update failed");
                        if(!p.reviewStatus.isEmpty()){ContentValues review=new ContentValues();review.put("source_key_status",p.reviewStatus);db.update("questions",review,"id=?",new String[]{String.valueOf(p.questionId)});}
                    }
                    db.setTransactionSuccessful();result.setImportedExplanations(pending.size());result.setStatus(PackImportResult.STATUS_SUCCESS);
                }finally{db.endTransaction();}
            }
        }catch(Exception error){result.addError(error.getMessage()==null?error.getClass().getSimpleName():error.getMessage());result.setStatus(PackImportResult.STATUS_FAILED);}
        return result;
    }
    public static String questionHash(String external,String text,String type)throws Exception {
        byte[] bytes=MessageDigest.getInstance("SHA-256").digest((external+"\n"+text+"\n"+type).getBytes(StandardCharsets.UTF_8));
        StringBuilder result=new StringBuilder();for(byte b:bytes)result.append(String.format(Locale.ROOT,"%02x",b&255));return result.toString();
    }
    private static String required(JSONObject object,String name)throws Exception {
        Object value=object.get(name);if(!(value instanceof String)||((String)value).trim().isEmpty())throw new IllegalArgumentException("Missing "+name);return (String)value;
    }
    private static final class Pending{final long id,questionId;final ContentValues values;final String reviewStatus;Pending(long id,ContentValues values,long questionId,String reviewStatus){this.id=id;this.values=values;this.questionId=questionId;this.reviewStatus=reviewStatus;}}
}
