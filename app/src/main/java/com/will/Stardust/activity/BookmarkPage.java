package com.will.Stardust.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.will.Stardust.Page.PageFactory;
import com.will.Stardust.R;
import com.will.Stardust.file.IReaderDB;

import java.util.ArrayList;

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
    private int currentWordNumber;
    private ArrayList<Integer> chaptersWordNumber;
    private int chapterNumber = 0;
    private MyAdapter bookmarkAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bookmark_view);
        bookName = getIntent().getStringExtra("name");
        bookPath = getIntent().getStringExtra("path");
        int begin = getIntent().getIntExtra("begin",-1);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        factory = new PageFactory();
        initializeList();
        factory.initializeBook(bookPath);
        list = (ListView) findViewById(R.id.bookmark_list_view);
        bookmarkAdapter = new MyAdapter(this);
        list.setAdapter(bookmarkAdapter);
        currentWordNumber = factory.getCurrentWordNumber(begin);
        try{
        chaptersWordNumber = iReaderDB.getChapterPosition(bookName);
        int temp;
        do{
            temp = chaptersWordNumber.get(chapterNumber++);
        }while(temp<currentWordNumber);
            list.setSelection(chapterNumber-2);
        }catch (Exception e){
            list.setSelection(0);
        }
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
                Toast.makeText(this,"请稍候···",Toast.LENGTH_SHORT).show();
                factory.setKeyWord("章");
                initializeChapter();
                break;
            case ADD_MODE_B:
                Toast.makeText(this,"请稍候···",Toast.LENGTH_SHORT).show();
                factory.setKeyWord("节");
                initializeChapter();
                break;
            case ADD_MODE_C:
                Toast.makeText(this,"请稍候···",Toast.LENGTH_SHORT).show();
                factory.setKeyWord("回");
                initializeChapter();
                break;
            case DELETE:
                Toast.makeText(this,"已删除",Toast.LENGTH_SHORT).show();
                iReaderDB.deleteBookmark(bookName);
                initializeList();
                bookmarkAdapter = new MyAdapter(this);
                list.setAdapter(bookmarkAdapter);
                addDialog.cancel();
                break;
        }
    }
    private void initializeChapter(){
        addDialog.cancel();
        iReaderDB.deleteBookmark(bookName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                chapters = factory.getChapter().toArray(new String[factory.getChapter().size()]);
                chaptersWordNumber = factory.getChapterPositions();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bookmarkAdapter = new MyAdapter(BookmarkPage.this);
                        list.setAdapter(bookmarkAdapter);
                        if(chaptersWordNumber.size()>0) {
                            chapterNumber = 0;
                            int temp;
                            do {
                                temp = chaptersWordNumber.get(chapterNumber++);
                            } while (temp < currentWordNumber);
                            list.setSelection(chapterNumber-2);
                    }}
                });
                for(int i = 0;i<chapters.length;i++){
                    iReaderDB.saveBookChapter(bookName,chapters[i],chaptersWordNumber.get(i));
                }

            }
        }).start();
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
    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        public MyAdapter (Context context){
            inflater = LayoutInflater.from(context);
        }
        public int getCount() {
            return chapters.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.bookmark_item,null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.bookmark_item);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.textView.setText(chapters[position]);
            if(position ==chapterNumber-2){
                holder.textView.setTextColor(Color.RED);
            }else{
                holder.textView.setTextColor(Color.rgb(37,37,37));
            }
            return convertView;
        }

    }
    private class ViewHolder{
        private TextView textView;
    }


}
