package com.will.Stardust.chapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.will.Stardust.PageFactory;
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
        ChapterFactory chapterFactory = new ChapterFactory();
        FastScrollRecyclerView recyclerView = (FastScrollRecyclerView) findViewById(R.id.chapter_activity_recycler_view);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.chapter_activity_refresh_layout);
        refreshLayout.setEnabled(false);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        final ChapterAdapter mAdapter = new ChapterAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chapterFactory.getChapter(new ChapterFactory.LoadCallback() {
            @Override
            public void onFinishLoad(List<Chapter> list) {
                mAdapter.addData(list);
                refreshLayout.setRefreshing(false);
            }
        });

        mAdapter.setOnItemClickListener(new ChapterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Chapter chapter) {
                Intent intent = new Intent();
                intent.putExtra("position",chapter.getChapterBytePosition());
                setResult(RESULT_OK,intent);
                finish();
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.chapter_activity_toolbar);
        toolbar.setTitle(PageFactory.getInstance().getBook().getBookName());
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
