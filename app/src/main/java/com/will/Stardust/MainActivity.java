package com.will.Stardust;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.will.Stardust.activity.adapter.BookListAdapter;
import com.will.Stardust.base.BaseActivity;
import com.will.Stardust.common.Util;
import com.will.Stardust.file_searcher.FileSearcherActivity;

/**
 * Created by will on 2016/10/29.
 */

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setAdapter(new BookListAdapter(this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_search){
            //Util.makeToast("you clicked search");
            startActivity(new Intent(this, FileSearcherActivity.class));
        }else{
            Util.makeToast("you clicked delete");
        }
        return true;
    }
}
