package com.will.Stardust.chapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.will.Stardust.R;
import com.will.Stardust.bean.Chapter;

import java.util.List;

/**
 * Created by will on 2016/11/6.
 */

public class ChapterAdapter extends RecyclerView.Adapter <ChapterAdapter.ChapterViewHolder>{
    private List<Chapter> data;

    public ChapterAdapter(List<Chapter> data){
        this.data = data;
    }

    @Override
    public void onBindViewHolder(ChapterViewHolder holder, int position) {
        holder.text.setText(data.get(position).getChapterName());
    }

    @Override
    public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item,parent,false);
        return new ChapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ChapterViewHolder extends RecyclerView.ViewHolder{
        TextView text;
        public ChapterViewHolder(View view){
            super(view);
            text = (TextView)view.findViewById(R.id.chapter_item_text);
        }
    }
}
