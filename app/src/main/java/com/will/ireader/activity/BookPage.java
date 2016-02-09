package com.will.ireader.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.will.ireader.Page.PageFactory;
import com.will.ireader.R;
import com.will.ireader.View.MenuAdapter;
import com.will.ireader.View.PageView;
import com.will.ireader.file.IReaderDB;

import java.io.File;

/**
 * Created by Will on 2016/2/2.
 */
public class BookPage extends Activity implements AdapterView.OnItemClickListener {
    private final int  GET_IMAGE = 1;
    private PageFactory pageFactory;
    private SharedPreferences.Editor editor;
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
    private AlertDialog menuDialog;//菜单dialog
    private View menuView;//菜单view;
    private Button increaseFont;//增大字体按钮
    private Button decreaseFont;//减小字体按钮
    private Button changeProgressConfirm;//进度跳转确认键
    private Button changeProgressCancel;//进度跳转取消键
    private EditText progressEditText;//进度跳转输入栏
    private EditText searchEditText;
    private TextView fontSizeDescription;//当前字体大小描述
    private AlertDialog fontChangeDialog;//调整字体大小dialog
    private AlertDialog progressChangeDialog;//进度跳转dialog
    private AlertDialog searchDialog;
    private Button searchContent;
    private Button nextContent;
    private Button confirmSkip;
    private Button returnToOriginPos;
    private final int DIRECTORY = 0;//章节目录
    private final int PROGRESS = 1;//进度跳转
    private final int FONT_SIZE = 2;//字体大小
    private final int BACKGROUND = 3;//设置背景
    private final int NIGHT_MODE = 4;//夜间模式
    private final int SEARCH = 5;//全文搜索
    private final int LINE_SPACEING = 6;//调整行距
    private final int RE_CODE = 7;//处理乱码\
    private MyListener myListener = new MyListener();
    private boolean cancelSearchDialogIndex = false;
    private String lastSearchContent = "";
    private int searchPosition;
    MyReceiver myReceiver;
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
        editor = sp.edit();
        fontSize = sp.getInt("font_size",45);
        pageFactory = new PageFactory(dm.heightPixels,dm.widthPixels,fontSize);
        try{
            bookName= this.getIntent().getStringExtra("name");
            bookPath = iReaderDB.getPath(bookName);
            position[0] =sp.getInt(bookName + "start", 0);
            position[1] = sp.getInt(bookName + "end", 0);
            String imagePath;
            if(!sp.getBoolean("night_mode",false)){
            if((imagePath = sp.getString("image_path","空")) == "空" || !(new File(imagePath).exists())) {
                pageFactory.setPageBackground(canvas,BitmapFactory.decodeResource(this.getResources(), R.drawable.book_bg11));
            }else {
                pageFactory.setPageBackground(canvas,BitmapFactory.decodeFile(imagePath));
            }
            }else{
                pageFactory.setNightMode(canvas,true);
            }
            pageFactory.openBook(bookPath, position);
            pageFactory.printPage(canvas, this);
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
        initializeReceiver();
        initializeMenuDialog();
    }
    @Override
    public void onItemClick(AdapterView<?>parent,View view,int position,long id){
        switch(position){
            case DIRECTORY:
                Intent intent  = new Intent(BookPage.this,BookmarkPage.class);
                intent.putExtra("name",bookName);
                intent.putExtra("path", bookPath);
                startActivity(intent);
                menuDialog.cancel();
                break;
            case FONT_SIZE:
                initializeFontDialog();
                fontChangeDialog.show();
                menuDialog.cancel();
                break;
            case PROGRESS:
                initializeProgressDialog();
                progressChangeDialog.show();
                menuDialog.cancel();
                break;
            case BACKGROUND:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,GET_IMAGE);
                menuDialog.cancel();
                break;
            case NIGHT_MODE:
                boolean nightMode = sp.getBoolean("night_mode",false);
                if (!nightMode) {
                    pageFactory.setNightMode(canvas,true);
                    editor.putBoolean("night_mode",true);
                    editor.commit();
                    pageView.invalidate();
                }else{
                    pageFactory.setNightMode(canvas,false);
                    editor.putBoolean("night_mode",false);
                    editor.commit();
                    pageView.invalidate();
                }
                break;
            case SEARCH:
                initializeSearchDialog();
                searchEditText.setText("");
                searchDialog.show();
                menuDialog.cancel();
                lastSearchContent = "";
                searchPosition = 0;
                pageFactory.saveNowPos();


        }

    }
    @Override
    protected void onPause(){
        super.onPause();
        position = pageFactory.getPosition();
        editor.putInt(bookName + "start", position[0]);
        editor.putInt(bookName + "end", position[1]);
        editor.putInt("font_size", pageFactory.getFontSize());
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

    class MyListener implements View.OnClickListener{
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.increase_font://增大字体
                pageFactory.setFontSize(pageFactory.getFontSize()+2);
                fontSizeDescription.setText("当前字号" + pageFactory.getFontSize());
                pageFactory.printPage(canvas );
                pageView.invalidate();
                break;
            case R.id.decrease_font://减小字体
                pageFactory.setFontSize(pageFactory.getFontSize()-2);
                fontSizeDescription.setText("当前字号" + pageFactory.getFontSize());
                pageFactory.printPage(canvas);
                pageView.invalidate();
                break;
            case R.id.change_progress_confirm://确定跳转进度
                String editTextContent = progressEditText.getText().toString();
                if(editTextContent != null && editTextContent != "");{
                pageFactory.setPercent(Float.parseFloat(editTextContent));
                pageFactory.printPage(canvas );
                pageView.invalidate();
                break;
            }
            case R.id.change_progress_cancel://取消跳转进度
                progressChangeDialog.cancel();
                break;
            case R.id.search_keyword_search:
                pageFactory.resetKeywordPos();
                String content = searchEditText.getText().toString();
                if(!content.equals(lastSearchContent)){
                int i = pageFactory.searchContent(canvas,content,"content");
                    if(i == -1){
                        Toast.makeText(BookPage.this,"未找到搜索项",Toast.LENGTH_SHORT).show();
                    }else{
                        pageView.invalidate();
                    }
                    lastSearchContent = content;
        }else{
                    Toast.makeText(BookPage.this,"你应该按『下一个』",Toast.LENGTH_SHORT).show();
                }
                Log.e(pageFactory.test,pageFactory.stringPosition +"");
                break;
            case R.id.search_keyword_next:
                if(!lastSearchContent.equals("")) {
                    if(searchPosition != -1) {
                       searchPosition = pageFactory.searchContent(canvas, " ", " ");
                        pageView.invalidate();
                        if(searchPosition == -1 ){
                            Toast.makeText(BookPage.this,"未找到搜索项",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(BookPage.this,"未找到搜索项",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(BookPage.this,"你应该先搜索",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.search_keyword_confirm:
                cancelSearchDialogIndex = true;
                searchDialog.cancel();
                break;
            case R.id.search_keyword_cancel:
                searchDialog.cancel();
                break;

        }
    }
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this,MainPageActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onDestroy(){
        unregisterReceiver(myReceiver);
        pageFactory.closeStream();
        super.onDestroy();
    }
    private void initializeProgressDialog(){
        if(progressChangeDialog == null){
        progressChangeDialog = new AlertDialog.Builder(this).create();
        View view  = View.inflate(this,R.layout.change_progress,null);
        progressEditText = (EditText) view.findViewById(R.id.change_progress_edit);
        changeProgressConfirm = (Button) view.findViewById(R.id.change_progress_confirm);
        changeProgressCancel = (Button) view.findViewById(R.id.change_progress_cancel);
            changeProgressCancel.setOnClickListener(myListener);
        changeProgressConfirm.setOnClickListener(myListener);
            progressChangeDialog.setView(view);
    }
    }
    private void initializeFontDialog(){
        //改变字体
        if(fontSizeDescription == null){
            fontChangeDialog = new AlertDialog.Builder(this).create();
            View changeFontView = View.inflate(this,R.layout.change_font_view,null);
            increaseFont = (Button) changeFontView.findViewById(R.id.increase_font);
            decreaseFont = (Button) changeFontView.findViewById(R.id.decrease_font);
            fontSizeDescription = (TextView) changeFontView.findViewById(R.id.font_size);
            fontSizeDescription.setText("当前字号"+pageFactory.getFontSize());
            fontChangeDialog.setView(changeFontView);
            increaseFont.setOnClickListener(myListener);
            decreaseFont.setOnClickListener((myListener));
        }
    }
    private void initializeMenuDialog(){
        if(menuDialog == null){
            menuView = View.inflate(this, R.layout.menu_view, null);
            MenuAdapter adapter = new MenuAdapter();
            GridView gridView = (GridView) menuView.findViewById(R.id.menu_grid_view);
            gridView.setAdapter(adapter.getMenuAdapter(this));
            menuDialog = new AlertDialog.Builder(this).create();
            menuDialog.setView(menuView);
            gridView.setOnItemClickListener(this);
        }
    }
    private void initializeReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("INVALIDATE_PAGEVIEW");
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver,intentFilter);

    }
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            int pos = intent.getIntExtra("pos",0);
            pageFactory.setPosition(pos);
            pageFactory.printPage(canvas );
            pageView.invalidate();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_IMAGE && resultCode ==RESULT_OK && data != null){
            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            String filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            //以下这部分，把图库中的文件复制了一份到软件目录中，后来又觉得没卵用，事实上好像确实没卵用。
            /*String fileDir = getFilesDir().getPath();
            String newFilePath = fileDir+"/"+new File(filePath).getName();
            File newFile = new File(newFilePath);
            File file = new File(fileDir);
            if(!file.exists()){
                file.mkdir();
            }
            if(!newFile.exists()){
            try{
            FileInputStream inputStream = new FileInputStream(filePath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream((new FileOutputStream(newFilePath)));
                byte[] temp = new byte[1024];
                int length = 0;
                while((length = bufferedInputStream.read(temp))>0){
                    bufferedOutputStream.write(temp,0,length);
                }
                bufferedInputStream.close();
                bufferedOutputStream.close();
            }catch(FileNotFoundException f){
                f.printStackTrace();
            }catch(IOException i ){
                i.printStackTrace();
            }
            }*/
            pageFactory.setNightMode(canvas,false);
            editor.putBoolean("night_mode",false);
            Bitmap bit = BitmapFactory.decodeFile(filePath);
            editor.putString("image_path",filePath);
            editor.commit();
            pageFactory.setPageBackground(canvas,bit);
            pageView.invalidate();
        }
    }
    private void initializeSearchDialog(){
        if(searchDialog == null){
        searchDialog = new AlertDialog.Builder(this).create();
        View view = View.inflate(this,R.layout.search_keyword,null);
        searchEditText = (EditText) view.findViewById(R.id.search_keyword_edit);
        searchContent =(Button) view.findViewById(R.id.search_keyword_search);
        nextContent = (Button) view.findViewById(R.id.search_keyword_next);
        confirmSkip = (Button) view.findViewById(R.id.search_keyword_confirm);
        returnToOriginPos = (Button) view.findViewById(R.id.search_keyword_cancel);
        searchContent.setOnClickListener(myListener);
        nextContent.setOnClickListener(myListener);
        confirmSkip.setOnClickListener(myListener);
        returnToOriginPos.setOnClickListener(myListener);
        searchDialog.setView(view);
        searchDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(!cancelSearchDialogIndex) {
                    pageFactory.returnToOriginPos(canvas);
                    pageView.invalidate();
                    cancelSearchDialogIndex = false;
                }
            }
        });
    }
    }
    }


