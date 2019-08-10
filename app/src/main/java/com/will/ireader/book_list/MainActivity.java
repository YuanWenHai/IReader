package com.will.ireader.book_list;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.will.filesearcher.FileSearcher;
import com.will.ireader.R;
import com.will.ireader.base.BaseActivity;
import com.will.ireader.common.SPHelper;
import com.will.ireader.common.Util;
import com.will.ireader.db.AppDatabase;
import com.will.ireader.book.Book;
import com.will.ireader.page.PageActivity;
import com.will.ireader.worker.AppWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * created  by will on 2019/8/6 10:50
 */
public class MainActivity extends BaseActivity {

    private BookListAdapter mAdapter;
    /**
     * 这里对Book对象采用了静态处理的策略，因为在Book对象当中储存了其阅读进度以及编码等信息，
     * 这些信息会在PageActivity中被修改，根据Intent序列化传参的特性，如果不静态化处理就会导致本activity中保存的Book与真实数据不一致，
     * 故在这里将所点击的Book对象置于Activity静态空间，便于book的同步.
     */
    public static Book CURRENT_BOOK;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView();
        detectDisplayType();
        initializeData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CURRENT_BOOK = null;
    }




    private void initializeView(){
        setContentView(R.layout.activity_book_list);
        RecyclerView recyclerView = findViewById(R.id.book_list_recycler_view);
        Toolbar toolbar = findViewById(R.id.book_list_toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        setActionBar(toolbar);
        mAdapter = new BookListAdapter();
        mAdapter.setClickListener((Book book) -> {
            CURRENT_BOOK = book;
            Intent startIntent = new Intent(this, PageActivity.class);
            startActivity(startIntent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
    }
    private void initializeData(){
        AppWorker.getInstance().runOnWorkerThread(() -> {
            List<Book> books = AppDatabase.getInstance(this).bookDao().getAllBooks();
            AppWorker.getInstance().runOnMainThread(() -> mAdapter.setBooks(books));
        });
    }

    /**
     * 获取设备屏幕是否为异形屏，并写入配置文件.
     * level28以上采用SDK内置API,反之则对statusBar进行计算，若statusBar之高度不等于24则认为其为异形屏.
     */
    private void detectDisplayType(){
        if(SPHelper.getInstance().getDisplayType() == null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                getWindow().getDecorView().post( ()-> {
                    boolean which = getWindow().getDecorView().getRootWindowInsets().getDisplayCutout() == null;
                    SPHelper.getInstance().setDisplayType(which ? SPHelper.DISPLAY_TYPE_NORMAL : SPHelper.DISPLAY_TYPE_NOTCHED);
                });
            }else{
                String displayType = SPHelper.DISPLAY_TYPE_NORMAL;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    int statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                    int standardStatusBarHeight = Util.getPXFromDP(24);
                    int offset = Util.getPXFromDP(1);
                    if(Math.abs(statusBarHeight - standardStatusBarHeight) > offset){
                        displayType = SPHelper.DISPLAY_TYPE_NOTCHED;
                    }
                }
                SPHelper.getInstance().setDisplayType(displayType);
            }
        }
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
                            mAdapter.addBooks(books);
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
}
