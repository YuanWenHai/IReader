package com.will.ireader.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.will.ireader.Page.PageFactory;
import com.will.ireader.R;
import com.will.ireader.View.MenuAdapter;
import com.will.ireader.View.PageView;
import com.will.ireader.file.IReaderDB;

/**
 * Created by Will on 2016/2/2.
 */
public class BookPage extends Activity implements AdapterView.OnItemClickListener,View.OnClickListener{
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
    private AlertDialog menuDialog;
    private View menuView;
    private Button increaseFont;
    private Button decreaseFont;
    private TextView fontSizeDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        pageView = new PageView(this);
        setContentView(pageView);
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        bitmap = Bitmap.createBitmap(dm.widthPixels,dm.heightPixels, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        fontSize = sp.getInt("font_size",30);
        pageFactory = new PageFactory(dm.heightPixels,dm.widthPixels,fontSize);
        try{
            bookName= this.getIntent().getStringExtra("name");
            bookPath = iReaderDB.getPath(bookName);
            position[0] =sp.getInt(bookName + "start", 0);
            position[1] = sp.getInt(bookName+"end",0);
            pageFactory.setPageBackground(BitmapFactory.decodeResource(this.getResources(), R.drawable.book_bg11));
            pageFactory.openBook(bookPath,position);
            pageFactory.printPage(canvas);
        }catch(Exception e){
            e.printStackTrace();
        }

        pageView.setBitmap(bitmap);
        pageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v == pageView) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getX() < dm.widthPixels / 3) {
                            Log.e("onTouch", "Left");
                            pageFactory.prePage();
                            pageFactory.printPage(canvas);
                        } else if (event.getX() > dm.widthPixels * 2 / 3) {
                            Log.e("onTouch", "Right");
                            pageFactory.nextPage();
                            pageFactory.printPage(canvas);
                        } else {
                            openOptionsMenu();
                        }
                        pageView.invalidate();
                    }
                }
                return false;
            }
        });
        menuView = View.inflate(this, R.layout.menu_view, null);
        MenuAdapter adapter = new MenuAdapter();
        GridView gridView = (GridView) menuView.findViewById(R.id.menu_grid_view);
        gridView.setAdapter(adapter.getMenuAdapter(this));
        menuDialog = new AlertDialog.Builder(this).create();
        menuDialog.setView(menuView);
        gridView.setOnItemClickListener(this);

    }
    @Override
    public void onItemClick(AdapterView<?>parent,View view,int position,long id){
        switch(position){
            case 0:
                Intent intent  = new Intent(BookPage.this,BookmarkPage.class);
                intent.putExtra("name",bookName);
                intent.putExtra("path",bookPath);
                startActivity(intent);
        }

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add("add");
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onMenuOpened(int FeatureId,Menu menu){
        if(menuDialog == null){
            menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
        }else{
            menuDialog.show();
        }
        return false;
    }
    private void initializeDialogs(){
        AlertDialog fontChangeDialog = new AlertDialog.Builder(this).create();
        View changeFontView = View.inflate(this,R.layout.change_font_view,null);
        increaseFont = (Button) changeFontView.findViewById(R.id.increase_font);
        decreaseFont = (Button) changeFontView.findViewById(R.id.decrease_font);
        fontSizeDescription = (TextView) changeFontView.findViewById(R.id.font_size);
        fontChangeDialog.setView(changeFontView);

    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.increase_font:
                //增大字体并刷新界面;
                break;
            case R.id.decrease_font:
                //缩小字体并刷新界面,还应改变textView中显示的数值;
                break;
        }
    }
    }


