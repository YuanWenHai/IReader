package com.will.Stardust.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.will.Stardust.R;
import com.will.Stardust.file.IReaderDB;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Will on 2016/2/17.
 */
public class DeleteBooks extends Activity implements View.OnClickListener{
    private IReaderDB iReaderDB;
    private ArrayList<String> bookNames;
    private HashMap<Integer,Boolean> checkStatus;
    private boolean allSelectedStatus = false;
    private Dialog deleteDialog;
    private CheckBox dialogCheckBox;
    private MyAdapter adapter;
    private ListView listView;
    Button dialogDeleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_books_view);
        iReaderDB = IReaderDB.getInstance(this);
        bookNames = iReaderDB.getBookName();
        Button confirmDeleteButton =(Button) findViewById(R.id.delete_books_confirm);
        Button selectAllButton = (Button) findViewById(R.id.delete_books_select_all);
        confirmDeleteButton.setOnClickListener(this);
        selectAllButton.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.delete_books_list);
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    deleteDialog = new Dialog(DeleteBooks.this);
                    deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100,200,200,200)));
                    View view = View.inflate(DeleteBooks.this, R.layout.delete_books_dialog, null);
                    dialogDeleteButton = (Button) view.findViewById(R.id.delete_books_dialog_confirm_button);
                    dialogCheckBox = (CheckBox) view.findViewById(R.id.delete_books_dialog_check_box);
                    dialogDeleteButton.setOnClickListener(DeleteBooks.this);
                    deleteDialog.setContentView(view);
                    deleteDialog.show();
            }
        });
    }
    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        public MyAdapter (Context context){
            inflater = LayoutInflater.from(context);
            checkStatus = new HashMap<Integer,Boolean>();
            for(int i = 0;i<bookNames.size();i++){
                checkStatus.put(i,false);
            }
        }
        public int getCount() {
            return bookNames.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.delete_books_item,null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.delete_books_text);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.delete_books_check_box);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.textView.setText(bookNames.get(position));
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkStatus.get(position)){
                        checkStatus.put(position,false);
                    }else{
                        checkStatus.put(position,true);
                    }
                }
            });
            if(checkStatus.get(position)){
                holder.checkBox.setChecked(true);
            }else{
                holder.checkBox.setChecked(false);
            }
            return convertView;
        }

    }
    private class ViewHolder{
        private TextView textView;
        private CheckBox checkBox;
    }
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.delete_books_select_all:
                if(allSelectedStatus){
                    for(int i = 0;i<checkStatus.size();i++){
                        checkStatus.put(i,false);
                        adapter.notifyDataSetChanged();
                        allSelectedStatus = false;
                    }
                }else{
                    for(int i = 0;i<checkStatus.size();i++){
                        checkStatus.put(i, true);
                        allSelectedStatus = true;
                        adapter.notifyDataSetChanged();
                }
                }
                break;
            case R.id.delete_books_dialog_confirm_button:
                deleteDialog.cancel();
                for(int i = 0;i<checkStatus.size();i++){
                    if(checkStatus.get(i)){
                        if(dialogCheckBox.isChecked()){
                            File file = new File(iReaderDB.getPath(bookNames.get(i)));
                            file.delete();
                        }
                        iReaderDB.deleteBook(bookNames.get(i));
                    }
                }
                bookNames = iReaderDB.getBookName();
                adapter = new MyAdapter(this);
                listView.setAdapter(adapter);
    }
}
}
