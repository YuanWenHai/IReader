package com.will.ireader.book_list;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.will.filesearcher.FileSearcher;
import com.will.ireader.R;
import com.will.ireader.base.BaseActivity;
import com.will.ireader.common.SPHelper;
import com.will.ireader.printer.AppDatabase;
import com.will.ireader.printer.Book;
import com.will.ireader.printer.PageActivity1;
import com.will.ireader.worker.AppWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * created  by will on 2019/8/6 10:50
 */
public class BookListActivity extends BaseActivity {

    private BookListAdapter mAdapter;
    public static Book CURRENT_BOOK;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        RecyclerView recyclerView = findViewById(R.id.book_list_recycler_view);
        Toolbar toolbar = findViewById(R.id.book_list_toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        setActionBar(toolbar);

        mAdapter = new BookListAdapter();
        mAdapter.setClickListener((Book book) -> {
            CURRENT_BOOK = book;
            Intent startIntent = new Intent(this, PageActivity1.class);
            startActivity(startIntent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        AppWorker.getInstance().runOnWorkerThread(() -> {
            List<Book> books = AppDatabase.getInstance(this).bookDao().getAllBooks();
            AppWorker.getInstance().runOnMainThread(() -> mAdapter.setBooks(books));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CURRENT_BOOK = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_menu_search:
                new FileSearcher(this)
                        .withExtension("txt")
                        .withSizeLimit(10*1024,-1)
                        .search((List<File> files) -> {
                            List<Book> books = new ArrayList<>();
                            for(File f: files){
                                books.add(new Book(f.getName(),f.getPath(),f.length()));
                            }
                            AppWorker.getInstance().runOnWorkerThread(() -> AppDatabase.getInstance(this).bookDao().addBook(books.toArray(new Book[]{})));
                        });
                break;
            case R.id.main_menu_management:
                //
                break;
            case R.id.main_menu_delete_all:
                //
                break;
            case R.id.main_menu_feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:a2265839@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,"阅读器建议/问题反馈");
                startActivity(emailIntent);
                break;
        }

        return true;
    }

    private void detectDisplayType(){

        if(SPHelper.getInstance().getDisplayType() == null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        boolean which = v.getRootWindowInsets().getDisplayCutout() == null;
                        SPHelper.getInstance().setDisplayType(which ? SPHelper.DISPLAY_TYPE_NORMAL : SPHelper.DISPLAY_TYPE_NOTCHED);
                        v.removeOnAttachStateChangeListener(this);
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {

                    }
                });
            }else{
                int statusBarHeight = 0;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

            }


        }


    }


}
