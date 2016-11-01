package com.will.Stardust.file_searcher;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.will.Stardust.R;
import com.will.Stardust.bean.Book;

import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class FSAdapter extends RecyclerView.Adapter<FSAdapter.FSViewHolder> {
    private Context context;
    private List<Book> list;
    private FileSearcher fileSearcher;
    private RecyclerView mRecyclerView;
    FSAdapter(Context context, final TextView textView,final List<Book> list){
        this.context = context;
        this.list = list;
        fileSearcher = new FileSearcher(list, new FileSearcher.Callback() {
            @Override
            public void onSearch(String pathName) {
                textView.setText("正在搜索"+pathName);
            }

            @Override
            public void onFind() {
                mRecyclerView.smoothScrollToPosition(list.size());
                notifyDataSetChanged();
            }
            @Override
            public void onFinish(){
                textView.setText("搜索完毕");
            }
        });
    }
    public void startSearch(){
        fileSearcher.startSearch();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public FSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_searcher_item,parent,false);
        return new FSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FSViewHolder holder, int position) {
        holder.text.setText(list.get(position).getBookName());
    }

    class FSViewHolder extends RecyclerView.ViewHolder{
        TextView text;
        FSViewHolder(View view){
            super(view);
            text = (TextView) view.findViewById(R.id.file_searcher_item_text);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }
}
