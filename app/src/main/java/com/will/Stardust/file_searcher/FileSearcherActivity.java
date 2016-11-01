package com.will.Stardust.file_searcher;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.will.Stardust.R;
import com.will.Stardust.base.BaseActivity;
import com.will.Stardust.bean.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class FileSearcherActivity extends BaseActivity {
    private List<Book> list = new ArrayList<>();
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_searcher);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.file_searcher_recycler_view);
        TextView header = (TextView) findViewById(R.id.file_searcher_header);
        final FSAdapter mAdapter = new FSAdapter(this,header,list);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.startSearch();
            }
        });
    }
}
