package com.will.ireader;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.will.ireader.bean.Book;
import com.will.ireader.common.SPHelper;
import com.will.ireader.common.Util;
import com.will.ireader.db.DBHelper;
import com.will.ireader.view.CustomLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by will on 2016/10/29.
 */

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {
    private List<Book> books;
    private Context mContext;
    private ClickCallback mCallback;
    private List<CustomLayout> mChildren = new ArrayList<>();
    private boolean isDeleteButtonVisible;

    public BookListAdapter(Context context){
        books = DBHelper.getInstance().getAllBook();
        mContext = context;
    }
    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Book book = books.get(position);
        String bookName = book.getBookName().substring(0,book.getBookName().lastIndexOf("."));
        holder.title.setText(bookName);
        holder.preview.setText(getPreview(book));
        //int progress = getProgress(book);
        int progress = (int)(new Random().nextFloat()*100);
        holder.root.setPercentage(progress);
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_book_item,parent,false);
        return new BookViewHolder(view);
    }

    public boolean isDeleteButtonVisible(){
        return isDeleteButtonVisible;
    }

    class BookViewHolder extends RecyclerView.ViewHolder{
        TextView title,preview;
        CustomLayout root;
        BookViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.main_book_item_title);
            preview = view.findViewById(R.id.main_book_item_preview);
            root = view.findViewById(R.id.main_book_item_root);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isDeleteButtonVisible()){
                        showDeleteButton(false);
                        return;
                    }
                    if(mCallback != null){
                        mCallback.onClick(books.get(getLayoutPosition()));
                    }
                }
            });
            root.setOnDeleteButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   deleteBook(getLayoutPosition());
                }
            });
            mChildren.add(root);
        }

    }
    public void showDeleteButton(boolean which){
        isDeleteButtonVisible = which;
        for (CustomLayout customLayout : mChildren){
            if(which){
                customLayout.showDeleteButton();
            }else{
                customLayout.hideDeleteButton();
            }

        }
    }

    private List<Book> getTestData(){
        List<Book> list = new ArrayList<>();
        Book book;
        for(int i=0;i<50;i++){
            book = new Book();
            book.setBookName(""+i);
            list.add(book);
        }
        return list;
    }


    //将选中的文件添加到列表及数据库中，因为存在对比操作，可能比较耗时，故这里新开一个线程执行并展示ProgressDialog
    public void addBookFromFile(final Context context, final List<File> files){
        final List<Book> newDataList = new ArrayList<>();
        final ProgressDialog dialog = Util.createProgressDialog(context,"处理中");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(File file :files){
                    if(!getBooks().contains(new Book(file.getName(),file.getPath()))){
                        newDataList.add(new Book(file.getName(),file.getPath()));
                    }
                }
                if(newDataList.size() == 0){
                    dialog.cancel();
                    return;
                }
                ((AppCompatActivity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        books.addAll(newDataList);
                        notifyDataSetChanged();
                        DBHelper.getInstance().saveBook(newDataList);
                    }
                });
                dialog.cancel();
            }
        }).start();
    }
    private void deleteBook(int position){
        Book deletedBook = books.remove(position);
        notifyItemRemoved(position);
        DBHelper.getInstance().deleteBookWithChapters(deletedBook);
    }

    public List<Book> getBooks(){
        return books;
    }
    public void clearData(){
        books.clear();
        notifyDataSetChanged();
    }
    private void reloadData(){
        books.clear();
        books.addAll(DBHelper.getInstance().getAllBook());
    }

    public void setOnClickCallback(ClickCallback callback){
        mCallback = callback;
    }
    public interface ClickCallback{
        void onClick(Book book);
        void onLongClick();
    }
    private String getPreview(Book book){
        String encoding = book.getEncoding();
        if(encoding == null){
            return "尚未阅读.";
        }
        String name = book.getBookName();
        int position = SPHelper.getInstance().getBookmarkStart(name);
        byte[] bytes = new byte[1024];
        String preview = "";
        File file = new File(book.getPath());
        try{
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
            randomAccessFile.skipBytes(position);
            randomAccessFile.read(bytes);
            preview = new String(bytes, encoding);
            randomAccessFile.close();
        }catch (FileNotFoundException f){
            f.printStackTrace();
            Log.e("file not found","!");
        }catch (IOException i ){
            i.printStackTrace();
        }
        return preview;
    }
    private int getProgress(Book book){
        File file = new File(book.getPath());
        if(file.exists() && file.isFile()){
            int currentPosition = SPHelper.getInstance().getBookmarkStart(book.getBookName());
            return currentPosition *100/ (int) Math.max(file.length(),1);
        }
        return 0;
    }
}
