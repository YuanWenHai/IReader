package com.will.ireader.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.will.ireader.Page.PageFactory;
import com.will.ireader.R;
import com.will.ireader.file.IReaderDB;

/**
 * Created by Will on 2016/2/4.
 */
public class BookmarkPage extends Activity implements AdapterView.OnItemClickListener{
    private String[] chapters;
    private String[] addItem = new String[]{"按章读取","按节读取","按回读取","删除目录"};
    private AlertDialog addDialog;
    private PageFactory factory;
    private final int ADD_MODE_A = 0;//按章添加
    private final int ADD_MODE_B = 1;//按节添加
    private final int ADD_MODE_C = 2;//按回添加
    private final int DELETE = 3;//删除
    private String bookName = "";
    private String bookPath = "";
    private IReaderDB iReaderDB;
    private ListView list;
    private SharedPreferences sp;
    ArrayAdapter<String> bookmarkAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bookmark_view);
        bookName = getIntent().getStringExtra("name");
        bookPath = getIntent().getStringExtra("path");
        sp = getSharedPreferences("config", MODE_PRIVATE);
        factory = new PageFactory(bookName,bookPath,BookmarkPage.this);
        initializeList();
        list = (ListView) findViewById(R.id.bookmark_list_view);
        bookmarkAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,chapters);
        list.setAdapter(bookmarkAdapter);
        addDialog = new AlertDialog.Builder(this).create();
        View dialogView = View.inflate(this,R.layout.bookmark_dialog,null);
        ListView dialogList = (ListView)dialogView.findViewById(R.id.add_list);
        addDialog.setView(dialogView);
        dialogList.setOnItemClickListener(this);
        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,addItem);
        dialogList.setAdapter(dialogAdapter);
        Button addBookmark = (Button) findViewById(R.id.bookmark_add);
        addBookmark.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v){
                addDialog.show();
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = factory.getPositionFromChapter(chapters[position]);
                Intent intent  = new Intent(BookmarkPage.this,BookPage.class);
                Intent broadcast = new Intent("INVALIDATE_PAGEVIEW");
                broadcast.putExtra("pos", pos);
                sendBroadcast(broadcast);
                startActivity(intent);
            }
        });
    }
    public void onItemClick(AdapterView<?> parent,View view,int position,long id){
        switch(position){
            case ADD_MODE_A:
                factory.setKeyWord("章");
                Toast.makeText(this,"正在加载，请稍等···",Toast.LENGTH_LONG).show();
                addDialog.cancel();
                iReaderDB.deleteBookmark(bookName);
                new Thread(new Runnable(){
                    @Override
                public void run(){
                        chapters = factory.getChapter().toArray(new String[factory.getChapter().size()]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookmarkAdapter = new ArrayAdapter<String>(BookmarkPage.this, android.R.layout.simple_expandable_list_item_1, chapters);
                                list.setAdapter(bookmarkAdapter);
                            }
                        });
                        for(int i = 0;i<chapters.length;i++){
                            iReaderDB.saveBookChapter(bookName,chapters[i]);
                        }
                    }
                }).start();
                break;
            case ADD_MODE_B:
                iReaderDB.deleteBookmark(bookName);
                factory.setKeyWord("节");
                Toast.makeText(this,"正在加载，请稍等···",Toast.LENGTH_LONG).show();
                addDialog.cancel();
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        chapters = factory.getChapter().toArray(new String[factory.getChapter().size()]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookmarkAdapter = new ArrayAdapter<String>(BookmarkPage.this,android.R.layout.simple_expandable_list_item_1,chapters);
                                list.setAdapter(bookmarkAdapter);
                            }
                        });
                        for(int i = 0;i<chapters.length;i++){
                            iReaderDB.saveBookChapter(bookName,chapters[i]);
                        }
                    }
                }).start();
                break;
            case ADD_MODE_C:
                factory.setKeyWord("回");
                Toast.makeText(this,"正在加载，请稍等···",Toast.LENGTH_LONG).show();
                addDialog.cancel();
                iReaderDB.deleteBookmark(bookName);
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        chapters = factory.getChapter().toArray(new String[factory.getChapter().size()]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookmarkAdapter = new ArrayAdapter<String>(BookmarkPage.this, android.R.layout.simple_expandable_list_item_1, chapters);
                                list.setAdapter(bookmarkAdapter);
                            }
                        });
                        for(int i = 0;i<chapters.length;i++){
                            iReaderDB.saveBookChapter(bookName,chapters[i]);
                        }

                    }
                }).start();
                break;

            case DELETE:
                Toast.makeText(this,"已删除",Toast.LENGTH_SHORT).show();
                iReaderDB.deleteBookmark(bookName);
                initializeList();
                bookmarkAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,chapters);
                list.setAdapter(bookmarkAdapter);
                addDialog.cancel();

        }
    }
    private void initializeList(){
        iReaderDB = IReaderDB.getInstance(this);
        chapters = iReaderDB.getBookChapter(bookName);
    }
    @Override
   public  void onBackPressed(){
        Intent intent = new Intent(this,BookPage.class);
        intent.putExtra("name", bookName);
        startActivity(intent);
        finish();

    }
    @Override
    public void onDestroy(){
        factory.closeStream();
        super.onDestroy();
    }

}
