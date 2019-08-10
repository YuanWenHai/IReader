package com.will.ireader.book_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.will.ireader.R;
import com.will.ireader.common.Util;
import com.will.ireader.book.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * created  by will on 2019/8/6 11:02
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookListViewHolder> {


    private List<Book> books;
    private BookListItemClickListener listener;

    public BookListAdapter(List<Book> books){
        this.books = books;
    }
    public BookListAdapter(){
        this.books = new ArrayList<>();
    }


    public void setBooks(List<Book> books){
        this.books.clear();
        this.books.addAll(books);
        notifyDataSetChanged();
    }

    public void addBooks(List<Book> list){
        this.books.addAll(list);
        notifyDataSetChanged();
    }

    public void setClickListener(BookListItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookListViewHolder holder, int position) {
        Book book = books.get(position);
        holder.name.setText(book.getName());
        holder.path.setText(book.getPath());
        holder.size.setText(Util.getFileSizeInUnit(book.getSize()));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }


    class BookListViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView path;
        TextView size;
        BookListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener((View v) -> {
                if(listener != null){
                    listener.onClick(books.get(getAdapterPosition()));
                }
            });
            name = itemView.findViewById(R.id.item_book_list_title);
            path = itemView.findViewById(R.id.item_book_list_path);
            size = itemView.findViewById(R.id.item_book_list_size);
        }
    }

    interface BookListItemClickListener{
        void onClick(Book book);
    }
}
