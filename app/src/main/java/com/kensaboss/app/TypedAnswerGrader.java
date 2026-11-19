package com.kensaboss.app;
import com.kensaboss.app.models.Question;
import com.kensaboss.app.models.CorrectAnswer;
import java.util.List;
/** Shared scoring for numeric answers and ordered matching/sequence answers. */
public final class TypedAnswerGrader {
    private TypedAnswerGrader() { }
    public static boolean supports(Question question){
        return question!=null&&(Question.TYPE_NUMERIC.equals(question.getQuestionType())||Question.TYPE_TEXT.equals(question.getQuestionType())||Question.TYPE_MATCHING.equals(question.getQuestionType())||Question.TYPE_SEQUENCE.equals(question.getQuestionType()));
    }
    public static Boolean evaluate(Question question,String value,List<CorrectAnswer> keys){
        if(question==null||!Question.KEY_VERIFIED.equals(question.getSourceKeyStatus())||keys==null||keys.isEmpty())return null;
        boolean matches=false;boolean ordered=Question.TYPE_MATCHING.equals(question.getQuestionType())||Question.TYPE_SEQUENCE.equals(question.getQuestionType());
        for(CorrectAnswer key:keys){
            if(!CorrectAnswer.STATUS_VERIFIED.equals(key.getVerificationStatus())||key.getAnswerValue()==null||CorrectAnswer.TYPE_OPTION.equals(key.getAnswerType()))return null;
            if(Question.TYPE_NUMERIC.equals(question.getQuestionType())){
                try{matches|=new java.math.BigDecimal(value.trim().replace(',','.')).compareTo(new java.math.BigDecimal(key.getAnswerValue().trim().replace(',','.')))==0;}catch(NumberFormatException ignored){}
            }else matches|=AnswerText.normalize(value,ordered).equals(AnswerText.normalize(key.getAnswerValue(),ordered));
        }
        return matches;
    }
}
