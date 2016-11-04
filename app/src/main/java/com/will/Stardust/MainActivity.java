package com.will.Stardust;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.will.Stardust.adapter.BookListAdapter;
import com.will.Stardust.base.BaseActivity;
import com.will.Stardust.bean.Book;
import com.will.Stardust.common.Util;
import com.will.filesearcher.file_searcher.FileSearcherActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class MainActivity extends BaseActivity {
    private static final int REQUEST_CODE = 888;

    private BookListAdapter mAdapter;

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        mAdapter = new BookListAdapter(this);
        mAdapter.setOnClickCallback(new BookListAdapter.ClickCallback() {
            @Override
            public void onClick(Book book) {
                Intent intent = new Intent(MainActivity.this,ReadingActivity.class);
                intent.putExtra("book",book);
                startActivity(intent);
            }
            @Override
            public void onLongClick() {
                turnIntoMoveMode(true);
            }
        });
        recyclerView.setAdapter(mAdapter);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                Intent intent = new Intent(this, FileSearcherActivity.class);
                intent.putExtra("keyword",".txt");
                startActivityForResult(intent,REQUEST_CODE);
                break;
            case R.id.menu_delete:
                if(mAdapter.isAllowMove()){
                  turnIntoMoveMode(false);
                }else{
                   turnIntoMoveMode(true);
                }
                break;
            case R.id.menu_delete_all:
                mAdapter.clearData();
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == FileSearcherActivity.OK && data != null){
            ArrayList<File> fileList = (ArrayList<File>) data.getSerializableExtra("data");
            List<Book> dataList = new ArrayList<>();
            for(File file :fileList){
                dataList.add(new Book(file.getName(),file.getPath()));
            }
            mAdapter.addData(dataList);
        }
    }

    @Override
    public void onBackPressed() {
        if(mAdapter.isAllowMove()){
           turnIntoMoveMode(false);
        }else{
            super.onBackPressed();
        }

    }
    private void turnIntoMoveMode(boolean which){
       if(which){
           mAdapter.allowMove(true);
           toolbar.setTitle(getResources().getString(R.string.remove_to_delete));
           Util.makeToast("进入删除模式，左右滑动书籍删除");
       }else{
           mAdapter.allowMove(false);
           toolbar.setTitle(getResources().getString(R.string.app_name));
           Util.makeToast("已退出删除模式");
       }
    }
}
