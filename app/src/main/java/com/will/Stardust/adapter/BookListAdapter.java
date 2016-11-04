package com.will.Stardust.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.will.RemovableView;
import com.will.Stardust.R;
import com.will.Stardust.bean.Book;
import com.will.Stardust.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {
    private List<Book> data;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private int lastAnimatedIndex = -1;
    private TranslateAnimation animation;
    private boolean allowMove;
    private ClickCallback mCallback;
    public BookListAdapter(Context context){
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
        holder.text.setText(data.get(position).getBookName());
        ((RemovableView)holder.itemView).disallowMove(!allowMove);
        //animate(holder.itemView,position);
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_book_item,parent,false);
        return new BookViewHolder(view);
    }


    class BookViewHolder extends RecyclerView.ViewHolder{
        public TextView text;
        BookViewHolder(View view){
            super(view);
            text = (TextView) view.findViewById(R.id.book_item_text);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCallback != null && !allowMove){//如果为删除模式则屏蔽点击事件
                        Book book = data.get(getAdapterPosition());
                        mCallback.onClick(book);
                        DBHelper.getInstance().updateBookAccessTime(book.setAccessTime(System.currentTimeMillis()));
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
       for(int i=0;i<list.size();i++){
           if(!data.contains(list.get(i))){
               temp.add(list.get(i));
           }
       }
        data.addAll(temp);
        notifyDataSetChanged();
        DBHelper.getInstance().saveBook(temp);
    }
    private void removeItem(int position){
        DBHelper.getInstance().deleteBookWithChapters(data.remove(position));
        lastAnimatedIndex = position -1;
        notifyDataSetChanged();
    }
    private void animate(final View view,int position){
        if(animation == null){
            animation = new TranslateAnimation(mRecyclerView.getWidth()-view.getWidth(),0,0,0);
            animation.setDuration(300);
        }
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
        DBHelper.getInstance().clearAllData();
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
        notifyDataSetChanged();
    }

    public void setOnClickCallback(ClickCallback callback){
        mCallback = callback;
    }
    public interface ClickCallback{
        void onClick(Book book);
        void onLongClick();
    }

}
