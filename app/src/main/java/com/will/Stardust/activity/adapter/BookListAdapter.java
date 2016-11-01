package com.will.Stardust.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.will.Stardust.R;
import com.will.Stardust.bean.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {
    private List<Book> data;
    private Context mContext;
    public BookListAdapter(Context context){
        //data = DBHelper.getInstance().getAllBook();
        data = getTestData();
        mContext = context;
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        holder.text.setText(data.get(position).getBookName());
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_book_item,parent,false);
        return new BookViewHolder(view);
    }


    class BookViewHolder extends RecyclerView.ViewHolder{
        private TextView text;
        BookViewHolder(View view){
            super(view);
            text = (TextView) view.findViewById(R.id.book_item_text);
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
}
