package com.will.ireader.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.will.ireader.Page.PageFactory;
import com.will.ireader.R;

import java.util.List;

/**
 * Created by Will on 2016/2/4.
 */
public class BookmarkPage extends Activity implements AdapterView.OnItemClickListener{
    private List<String>  BookmarkList;
    private String[] bookmarkData = new String[]{"1","2"};
    private String[] addItem = new String[]{"按章读取","按节读取","按回读取"};
    private AlertDialog addDialog;
    private PageFactory factory;
    private final int ADD_MODE_A = 0;//按章添加
    private final int ADD_MODE_B = 1;//按节添加
    private final int ADD_MODE_C = 2;//按回添加
    private String bookName = "";
    private String bookPath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bookmark_view);
        bookName = getIntent().getStringExtra("name");
        bookPath = getIntent().getStringExtra("path");
        ListView list = (ListView) findViewById(R.id.bookmark_list_view);
        ArrayAdapter<String> bookmarkAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,bookmarkData);
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
    }
    public void onItemClick(AdapterView<?> parent,View view,int position,long id){
        switch(position){
            case ADD_MODE_A:
                factory = new PageFactory(bookName,bookPath,BookmarkPage.this);
                factory.getChapter();
        }
    }

}
