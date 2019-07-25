package com.will.filesearcher;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.will.filesearcher.filter.FileFilter;
import com.will.filesearcher.searchengine.FileItem;
import com.will.filesearcher.searchengine.SearchEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2017/10/31.
 */

public class FileSearcherActivity extends AppCompatActivity{
    private SearchEngine searchEngine;
    private FileSearcherAdapter adapter;
    private Toolbar toolbar;
    private List<FileItem> selectedItems;
    private View emptyView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_searcher_main);
        initializeSearchEngine();
        initializeView();
        searchEngine.start();
    }


    private void initializeView(){
        emptyView = findViewById(R.id.file_searcher_main_no_result_found);
        toolbar = findViewById(R.id.file_searcher_main_toolbar);
        toolbar.setNavigationIcon(R.drawable.back_holo_dark_no_trim_no_padding);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        FloatingActionButton fab = findViewById(R.id.file_searcher_main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchEngine.isSearching()){
                    return;
                }
                ArrayList<File> list = new ArrayList<>();
                if(selectedItems != null){
                    for(FileItem item : selectedItems){
                        list.add(item.getFile());
                    }
                }
                FileSearcher.callback.onSelect(list);
                finish();
            }
        });
        RecyclerView recyclerView  = findViewById(R.id.file_searcher_main_recycler_view);
        adapter = new FileSearcherAdapter(this,searchEngine);
        adapter.setOnItemSelectCallback(new FileSearcherAdapter.OnItemSelectCallback() {

            @Override
            public void onSelectStateChanged(List<FileItem> items) {
                if(selectedItems == null){
                    selectedItems = items;
                }
                toolbar.setTitle(selectedItems.size()+"/"+adapter.getItemCount());
            }


        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }
    private void initializeSearchEngine(){
        FileFilter filter = (FileFilter) getIntent().getSerializableExtra(FileSearcher.FILE_FILTER);
        File path = (File) getIntent().getSerializableExtra(FileSearcher.SEARCH_PATH);
        if(filter == null || path == null){
            throw new NullPointerException("the filter and path cannot be null!");
        }
        searchEngine = new SearchEngine(path,filter);
        searchEngine.setCallback(new SearchEngine.SearchEngineCallback() {
            @Override
            public void onFind(List<FileItem> items) {
                adapter.addItem(items);
            }

            @Override
            public void onSearchDirectory(File file) {
                toolbar.setSubtitle( file.getPath().replace(Environment.getExternalStorageDirectory().getPath()+File.separator,""));
            }

            @Override
            public void onFinish() {
                if(adapter.getItemCount() == 0){
                    emptyView.setVisibility(View.VISIBLE);
                }
                toolbar.setTitle("0/"+adapter.getItemCount());
                toolbar.setSubtitle(getString(com.will.filesearcher.R.string.file_searcher_searching_completed));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(searchEngine.isSearching()){
            showCancelDialog();
        }else{
            super.onBackPressed();
        }
    }
    private void showCancelDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.file_searcher_cancel_search)).
                setPositiveButton(getString(R.string.file_searcher_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        searchEngine.stop();
                    }
                }).setNegativeButton(getString(R.string.file_searcher_cancel),null)
                .create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.will.filesearcher.R.menu.file_searcher_activity_menu,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileSearcher.callback = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == com.will.filesearcher.R.id.file_searcher_menu_select_all && !searchEngine.isSearching()){

            adapter.selectAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
