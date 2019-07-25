package com.will.filesearcher;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.will.filesearcher.delegate.FileSearcherDelegateActivity;
import com.will.filesearcher.filter.FileFilter;

import java.io.File;
import java.util.List;

/**
 * Created by Will on 2017/10/31.
 */

// TODO: 2017/12/6  在delegateActivity申请权限时似乎会有短暂的黑屏，应注意解决
public class FileSearcher  {
    private File path;
    static final String FILE_FILTER = "file_filter";
    static final String SEARCH_PATH = "search_path";
    private final FileFilter fileFilter = new FileFilter();
    private final Context context;
    public static FileSearcherCallback callback;

    /**
     *
     * @param context context
     */
    public FileSearcher(@NonNull Context context){
        this.context = context;
    }

    /**
     * search with detail limit
     * @param min minimum size in byte
     * @param max max size in byte,negative value is no limit
     * @return itself
     */
    public FileSearcher withSizeLimit(long min, long max){
        fileFilter.withSizeLimit(min,max);
        return this;
    }

    /**
     * search with extension
     * @param extension  extension,such as txt,jpg.
     * @return itself
     */
    public FileSearcher withExtension(@NonNull String extension){
        fileFilter.withExtension(extension);
        return this;
    }

    /**
     * search with keyword
     * @param keyword keyword
     * @return itself
     */
    public FileSearcher withKeyword(@NonNull String keyword){
        fileFilter.withKeyword(keyword);
        return this;
    }

    /**
     * whether show hidden files or not(whether show files that prefix with '.'),default is not.
     * @param showHidden show or not
     * @return itself
     */
    public FileSearcher showHidden(boolean showHidden){
        fileFilter.showHidden(showHidden);
        return this;
    }
    public FileSearcher withRootPath(File path){
        this.path = path;
        return this;
    }

    /**
     * search with specified conditions,if passed path is invalid,an IllegalStatementException will be thrown.
     * @param callback
     */
    public void search(FileSearcherCallback callback){
        if(path == null){
            path = Environment.getExternalStorageDirectory();
        }else if(!path.isDirectory()){
            throw new IllegalArgumentException("the path must be a directory");
        }
        this.callback = callback;
        Intent intent = new Intent(context,FileSearcherDelegateActivity.class);
        intent.putExtra(FILE_FILTER,fileFilter);
        intent.putExtra(SEARCH_PATH,path);
        context.startActivity(intent);
    }
    public interface FileSearcherCallback{
        void onSelect(List<File> files);
    }
}
