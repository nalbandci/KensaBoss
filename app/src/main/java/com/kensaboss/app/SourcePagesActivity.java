package com.kensaboss.app;

import android.content.Context;
import android.graphics.*;
import android.graphics.pdf.PdfRenderer;
import android.os.*;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.kensaboss.app.localization.*;
import java.io.*;
import java.util.concurrent.*;

/** Offline original-page reader; formulas and drawings remain faithful to the book. */
public class SourcePagesActivity extends AppCompatActivity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private int pageIndex;private int count;private long request;private String path;
    private PageView image;private TextView title;private WebView web;
    @Override protected void attachBaseContext(Context base){super.attachBaseContext(FontSizeManager.wrap(LocaleManager.wrap(base)));}
    @Override protected void onCreate(Bundle saved){
        ThemeManager.applyTheme(this);super.onCreate(saved);
        long id=getIntent().getLongExtra("question_id",-1);boolean lesson=getIntent().getBooleanExtra("lesson",false);
        path=StudyContent.resource(this,id,lesson);
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setPadding(12,24,12,12);layout.setBackgroundColor(getColor(R.color.kb_background));
        Button back=new Button(this);back.setText("Հետ");back.setOnClickListener(v->finish());layout.addView(back);setContentView(layout);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(layout,(view,insets)->{
            androidx.core.graphics.Insets bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            view.setPadding(12+bars.left,24+bars.top,12+bars.right,12+bars.bottom);return insets;
        });
        new androidx.core.view.WindowInsetsControllerCompat(getWindow(),getWindow().getDecorView()).setAppearanceLightStatusBars(androidx.core.graphics.ColorUtils.calculateLuminance(getColor(R.color.kb_background))>.5);
        if(path==null){TextView error=new TextView(this);error.setText("Նյութը հասանելի չէ։");layout.addView(error);return;}
        if(lesson){
            web=new WebView(this);web.getSettings().setJavaScriptEnabled(false);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);web.getSettings().setBlockNetworkLoads(true);
            layout.addView(web,new LinearLayout.LayoutParams(-1,0,1));
            try{web.loadDataWithBaseURL(null,readUtf8(path),"text/html","UTF-8",null);}catch(IOException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}return;
        }
        try(ParcelFileDescriptor fd=ParcelFileDescriptor.open(new File(path),ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer renderer=new PdfRenderer(fd)){count=renderer.getPageCount();}
        catch(IOException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
        pageIndex=Math.max(0,Math.min(count-1,saved==null?getIntent().getIntExtra("page",1)-1:saved.getInt("page",0)));
        title=new TextView(this);title.setTextColor(getColor(R.color.kb_text_primary));layout.addView(title);
        LinearLayout nav=new LinearLayout(this);
        Button previous=new Button(this);previous.setText("◀");previous.setOnClickListener(v->{if(pageIndex>0){pageIndex--;render();}});nav.addView(previous,new LinearLayout.LayoutParams(0,-2,1));
        EditText go=new EditText(this);go.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);go.setHint("Էջ");nav.addView(go,new LinearLayout.LayoutParams(0,-2,1));
        Button jump=new Button(this);jump.setText("Բացել");jump.setOnClickListener(v->{try{int n=Integer.parseInt(go.getText().toString());if(n>=1&&n<=count){pageIndex=n-1;render();}}catch(NumberFormatException ignored){}});nav.addView(jump,new LinearLayout.LayoutParams(0,-2,1));
        Button next=new Button(this);next.setText("▶");next.setOnClickListener(v->{if(pageIndex<count-1){pageIndex++;render();}});nav.addView(next,new LinearLayout.LayoutParams(0,-2,1));layout.addView(nav);
        image=new PageView(this);layout.addView(image,new LinearLayout.LayoutParams(-1,0,1));render();
    }
    private void render(){
        title.setText("Բնօրինակ · էջ "+(pageIndex+1)+" / "+count+" · Մեծացրու երկու մատով");
        final int index=pageIndex;final long ticket=++request;
        worker.execute(()->{
            try(ParcelFileDescriptor fd=ParcelFileDescriptor.open(new File(path),ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer renderer=new PdfRenderer(fd);PdfRenderer.Page page=renderer.openPage(index)){
                int width=1600,height=Math.round(width*(float)page.getHeight()/page.getWidth());
                Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);bitmap.eraseColor(Color.WHITE);page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                runOnUiThread(()->{if(isDestroyed()||ticket!=request){bitmap.recycle();return;}image.setBitmap(bitmap);});
            }catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();});}
        });
    }
    private String readUtf8(String file)throws IOException{
        try(InputStream in=new FileInputStream(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>2*1024*1024)throw new IOException("Lesson too large");out.write(buffer,0,n);}return out.toString("UTF-8");
        }
    }
    @Override protected void onSaveInstanceState(Bundle out){out.putInt("page",pageIndex);super.onSaveInstanceState(out);}
    @Override protected void onDestroy(){request++;worker.shutdownNow();if(image!=null)image.clear();if(web!=null)web.destroy();super.onDestroy();}
    private static class PageView extends View {
        private Bitmap bitmap;private float scale=1,dx,dy,lastX,lastY;private final ScaleGestureDetector zoom;
        PageView(Context c){super(c);zoom=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector d){float old=scale;scale=Math.max(.15f,Math.min(5f,scale*d.getScaleFactor()));float factor=scale/old;dx=d.getFocusX()-(d.getFocusX()-dx)*factor;dy=d.getFocusY()-(d.getFocusY()-dy)*factor;invalidate();return true;}});}
        void setBitmap(Bitmap b){clear();bitmap=b;scale=getWidth()>0?(float)getWidth()/b.getWidth():.5f;dx=dy=0;invalidate();}
        void clear(){if(bitmap!=null){bitmap.recycle();bitmap=null;}}
        @Override protected void onDraw(Canvas c){super.onDraw(c);if(bitmap!=null){c.save();c.translate(dx,dy);c.scale(scale,scale);c.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));c.restore();}}
        @Override public boolean onTouchEvent(MotionEvent e){zoom.onTouchEvent(e);if(e.getActionMasked()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();}else if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&!zoom.isInProgress()&&e.getPointerCount()==1){dx+=e.getX()-lastX;dy+=e.getY()-lastY;invalidate();}lastX=e.getX();lastY=e.getY();return true;}
    }
}
