package com.will.ireader.printer;

import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.will.ireader.R;
import com.will.ireader.base.BaseActivity;
import com.will.ireader.book_list.BookListActivity;
import com.will.ireader.worker.AppWorker;

import java.util.List;

/**
 * created  by will on 2019/7/5 15:43
 */
public class PageActivity1 extends BaseActivity {

    public static String PARAM_BOOK = "book";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_page_1);
        Book book = BookListActivity.CURRENT_BOOK;
        //Log.e("book",new Gson().toJson(book));
        book.initialize();
        Page page = findViewById(R.id.page_view);
        page.setPrinter(new Printer(book));
    }
}
