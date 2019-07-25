package com.will.ireader;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.will.filesearcher.FileSearcher;
import com.will.ireader.base.BaseActivity;
import com.will.ireader.bean.Book;
import com.will.ireader.common.SPHelper;
import com.will.ireader.common.Util;
import com.will.ireader.db.DBHelper;
import com.will.ireader.printer.PageActivity1;

import java.io.File;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class MainActivity extends BaseActivity {
    private static final int RESTART_REQUEST = 123;
    private Toast mToast;
    private BookListAdapter mAdapter;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(SPHelper.getInstance().isNightMode() ? R.style.AppNightTheme : R.style.AppDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();

    }


    private void initializeView(){
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new BookListAdapter(this);
        mAdapter.setOnClickCallback(new BookListAdapter.ClickCallback() {
            @Override
            public void onClick(final Book book) {
                //Log.e("test",printer.printLineForward(50));
                com.will.ireader.printer.Book book1 = new com.will.ireader.printer.Book(book.getBookName(),book.getPath());
                Intent startIntent = new Intent(MainActivity.this, PageActivity1.class);
                startIntent.putExtra(PageActivity1.PARAM_BOOK,book1);
                startActivity(startIntent);
               /* book.setEncoding("gbk");

                final PageInfo info = new PageInfo(book);
                info.prepare(new PageInfo.ReadCallback() {
                    @Override
                    public void onStart() {
                        Util.createProgressDialog(MainActivity.this,"读取中...");
                    }

                    @Override
                    public void onSuccess() {
                        Log.e("encoding",book.getEncoding());
                        Intent intent = new Intent(MainActivity.this, PageActivity.class);
                        if(book.getEncoding() == null){
                            book.setEncoding(Util.getEncoding(book));
                            DBHelper.getInstance().updateBook(book);
                        }
                        intent.putExtra(PageInfo.PAGE_INFO,info);
                        startActivityForResult(intent,RESTART_REQUEST);
                    }

                    @Override
                    public void onBookInvalid() {

                    }
                });*/
            }
            @Override
            public void onLongClick() {
                //turnIntoMoveMode(true);
            }
        });
        recyclerView.setAdapter(mAdapter);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
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
                        .search(new FileSearcher.FileSearcherCallback() {
                            @Override
                            public void onSelect(final List<File> files) {
                                mAdapter.addBookFromFile(MainActivity.this,files);
                            }
                        });
                break;
            case R.id.main_menu_management:
                mAdapter.showDeleteButton(!mAdapter.isDeleteButtonVisible());
                break;
            case R.id.main_menu_delete_all:
                showDeleteAllDialog();
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

    @Override
    protected void onRestart() {
        super.onRestart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RESTART_REQUEST && resultCode == Activity.RESULT_OK){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(mToast == null){
            mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        }
        if (mAdapter.isDeleteButtonVisible()){
            mAdapter.showDeleteButton(false);
        }else if(mToast.getView().getParent() == null){
            mToast.setText("再次点击返回退出应用");
            mToast.show();
        }else{
            super.onBackPressed();
        }
    }
    private void showDeleteAllDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AppDialogTheme);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdapter.clearData();
                DBHelper.getInstance().clearAllData();
                SPHelper.getInstance().clearAllBookMarkData();
                Util.makeToast("已删除");
            }
        });
        builder.setMessage("确认删除全部书籍？");
        builder.setNegativeButton("取消",null);
        builder.show();
    }
}
