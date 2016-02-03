package com.will.ireader.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.will.ireader.Page.PageFactory;
import com.will.ireader.R;
import com.will.ireader.View.PageView;
import com.will.ireader.file.IReaderDB;

/**
 * Created by Will on 2016/2/2.
 */
public class BookPage extends Activity {
    private PageFactory pageFactory;
    private Bitmap bitmap;
    private Canvas canvas;
    private DisplayMetrics dm;
    private SharedPreferences sp;
    private LayoutInflater inflater;
    private PageView pageView;
    private int fontSize;
    private String bookName ;
    private String bookPath;
    private int[] position = {0,0};
    private IReaderDB iReaderDB  = IReaderDB.getInstance(this);
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        pageView = new PageView(this);
        setContentView(pageView);
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        bitmap = Bitmap.createBitmap(dm.widthPixels,dm.heightPixels, Bitmap.Config.ARGB_8888);
        Log.e("height:"+dm.heightPixels,"width"+dm.widthPixels);
        canvas = new Canvas(bitmap);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        fontSize = sp.getInt("font_size",30);
        pageFactory = new PageFactory(dm.heightPixels,dm.widthPixels,fontSize);
        try{
            Log.e("printPage","1");
            bookName= this.getIntent().getStringExtra("name");
            Log.e("book_name",bookName);
            bookPath = iReaderDB.getPath(bookName);
            Log.e("book_path",bookPath);
            position[0] =sp.getInt(bookName + "start", 0);
            position[1] = sp.getInt(bookName+"end",0);
            Log.e("position",position[0]+"");
            pageFactory.setPageBackground(BitmapFactory.decodeResource(this.getResources(), R.drawable.book_bg11));
            pageFactory.openBook(bookPath,position);
            Log.e("after open book"," ");
            pageFactory.printPage(canvas);
            Log.e("printPage","printPage");
        }catch(Exception e){
            e.printStackTrace();
        }

        pageView.setBitmap(bitmap);
        pageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v ==pageView){
                    if(event.getAction() ==MotionEvent.ACTION_DOWN ){
                        if(event.getX()<dm.widthPixels/3){
                            Log.e("onTouch","Left");
                            pageFactory.prePage();
                            pageFactory.printPage(canvas);
                        }else if (event.getX()>dm.widthPixels*2/3){
                            Log.e("onTouch","Right");
                            pageFactory.nextPage();
                            pageFactory.printPage(canvas);
                        }else{
                            //菜单事件
                        }
                        pageView.invalidate();
                    }
                }
                return false;
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
        position = pageFactory.getPosition();
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(bookName+"start",position[0]);
        editor.putInt(bookName+"end",position[1]);
        editor.putInt("font_size",fontSize);
        editor.commit();
    }

}
