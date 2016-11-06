package com.will.Stardust.chapter;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.will.Stardust.R;
import com.will.Stardust.base.BaseActivity;
import com.will.Stardust.bean.Chapter;

import java.util.List;

/**
 * Created by will on 2016/11/6.
 */

public class ChapterActivity extends BaseActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
        List<Chapter> list = ChapterFactory.getInstance().getChapterList();
        ChapterFactory.getInstance().recycle();
        FastScrollRecyclerView recyclerView = (FastScrollRecyclerView) findViewById(R.id.chapter_activity_recycler_view);
        recyclerView.setAdapter(new ChapterAdapter(list));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.chapter_activity_toolbar);
        toolbar.setTitle(list.get(0).getBookName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
