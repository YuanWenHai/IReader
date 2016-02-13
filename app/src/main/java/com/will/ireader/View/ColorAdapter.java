package com.will.ireader.View;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.will.ireader.R;

/**
 * Created by Will on 2016/2/10.
 */
public class ColorAdapter extends BaseAdapter {
    private final int[] color = new int[]{Color.WHITE,Color.RED,Color.LTGRAY,Color.BLACK,Color.BLUE,
            Color.CYAN, Color.DKGRAY,Color.GRAY};
    private LayoutInflater inflater;
    public ColorAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount(){
        return color.length;
    }
    @Override
    public Object getItem(int position){
        return null;
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.choose_color_item,null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.choose_color_image);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.imageView.setBackgroundColor(color[position]);
        return convertView;
    }
    class ViewHolder{
        ImageView imageView;
    }
    }

