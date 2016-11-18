package com.will.Stardust;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.will.RemovableView;
import com.will.Stardust.bean.Book;
import com.will.Stardust.common.SPHelper;
import com.will.Stardust.db.DBHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.BookViewHolder> {
    private List<Book> data;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private int lastAnimatedIndex = -1;
    private boolean allowMove;
    private ClickCallback mCallback;
    public MainAdapter(Context context){
        data = DBHelper.getInstance().getAllBook();
        //data = getTestData();
        mContext = context;
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Book book = data.get(position);
        String bookName = book.getBookName().substring(0,book.getBookName().lastIndexOf("."));
        holder.title.setText(bookName);
        holder.preview.setText(getPreview(book));
        holder.progressBar.setMax(100);
        int progress = getProgress(book);
        holder.progressBar.setProgress(progress);
        holder.progressText.setText(progress+"%");
        if(allowMove){
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryLightest));
        }else{
            holder.cardView.setCardBackgroundColor(null);
        }
        ((RemovableView)holder.itemView).disallowMove(!allowMove);
        animate(holder.itemView,position);
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_book_item,parent,false);
        return new BookViewHolder(view);
    }


    class BookViewHolder extends RecyclerView.ViewHolder{
        public TextView title,preview,progressText;
        public ProgressBar progressBar;
        public CardView cardView;
        BookViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.main_book_item_title);
            preview =(TextView) view.findViewById(R.id.main_book_item_preview);
            progressText = (TextView) view.findViewById(R.id.main_book_item_progress_text);
            progressBar = (ProgressBar) view.findViewById(R.id.main_book_item_progress_bar);
            cardView = (CardView) view.findViewById(R.id.main_book_item_card_view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCallback != null && !allowMove){//如果为删除模式则屏蔽点击事件
                        Book book = data.get(getAdapterPosition());
                        mCallback.onClick(book);
                        DBHelper.getInstance().updateBook(book.setAccessTime(System.currentTimeMillis()));
                        reloadData();
                    }
                }
            });
            ((RemovableView)view).setOnRemoveCallback(new RemovableView.OnRemoveCallback() {
                @Override
                public void onRemove(View view) {
                    removeItem(getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(mCallback != null & !allowMove){
                        mCallback.onLongClick();
                    }
                    return true;
                }
            });
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
    //效率有点低，但一时间想不到更高效的对比方法
    public void addData(List<Book> list){
        List<Book> temp = new ArrayList<>();
        for (Book book :list){
            if(!data.contains(book)){
                temp.add(book);
            }
        }
        data.addAll(temp);
        DBHelper.getInstance().saveBook(temp);
        notifyDataSetChanged();
    }

    public List<Book> getData(){
        return data;
    }
    private void removeItem(int position){
        Book book = data.remove(position);
        DBHelper.getInstance().deleteBookWithChapters(book);
        SPHelper.getInstance().deleteBookMark(book.getBookName());
        lastAnimatedIndex = position -1;
        notifyDataSetChanged();
    }
    private void animate(final View view,int position){
        final TranslateAnimation animation = new TranslateAnimation(view.getWidth(),0,0,0);
        animation.setDuration(300);
        if(position > lastAnimatedIndex){
            view.setVisibility(View.INVISIBLE);
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(animation);
                    view.setVisibility(View.VISIBLE);
                }
            },100);
            lastAnimatedIndex = position;
        }
    }
    public void clearData(){
        data.clear();
        notifyDataSetChanged();
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }
    public void allowMove(boolean which){
        allowMove = which;
        notifyDataSetChanged();
    }
    public boolean isAllowMove(){
        return allowMove;
    }

    private void reloadData(){
        data.clear();
        data.addAll(DBHelper.getInstance().getAllBook());
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
