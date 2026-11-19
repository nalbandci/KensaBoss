package com.kensaboss.app;

/** Ordered numeric lists are compared by tokens, ordinary text keeps its punctuation. */
public final class AnswerText {
    private AnswerText() { }
    public static String normalize(String text,boolean ordered){
        if(text==null)return "";
        if(ordered)return text.trim().replaceAll("[\\s,;→\\-]+",",").replaceAll("^,|,$","");
        return text.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
