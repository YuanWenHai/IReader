package com.will.ireader.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.will.ireader.R;
import com.will.ireader.file.FileSelector;
import com.will.ireader.file.IReaderDB;

import java.io.File;
import java.util.List;

/**
 * Created by Will on 2016/1/29.
 */
public class MainPageActivity extends Activity {
    List<String> bookList;
    List<String> pathList;
    IReaderDB iReaderDB;
    ListView listView;
    Button add;
    CheckBox checkBox;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_page);
        iReaderDB = IReaderDB.getInstance(this);
        bookList = iReaderDB.getBookName();
        pathList = iReaderDB.getBookPath();
        add = (Button) findViewById(R.id.add_button);
        listView = (ListView)findViewById(R.id.list_view);
        MainPageAdapter adapter = new MainPageAdapter(this);
        listView.setAdapter(adapter);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, FileSelector.class);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainPageActivity.this);
                dialog.setTitle("删除");
                dialog.setMessage("确定删除" + bookList.get(position) + "?");
                LayoutInflater inflater = getLayoutInflater().from(MainPageActivity.this);
                View dialogView = inflater.inflate(R.layout.delete_dialog, null);
                dialog.setView(dialogView);
                checkBox = (CheckBox) dialogView.findViewById(R.id.dialog_check_box);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkBox.isChecked()) {
                            //删除本地文件；
                            File file = new File(iReaderDB.getPath(bookList.get(position)));
                            file.delete();
                        }
                        String bookName = iReaderDB.getBookName().get(position);
                        iReaderDB.deleteBook(bookName);
                        bookList = iReaderDB.getBookName();
                        MainPageAdapter adapter = new MainPageAdapter(MainPageActivity.this);
                        listView.setAdapter(adapter);

                    }
                });
                dialog.show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = bookList.get(position);
                Intent intent = new Intent(MainPageActivity.this,BookPage.class);
                intent.putExtra("name",name);
                startActivity(intent);
            }
        });
    }
    class MainPageAdapter extends BaseAdapter {
        LayoutInflater inflater;
        public MainPageAdapter (Context context){
            inflater = LayoutInflater.from(context);
        }
        public int getCount() {
            return bookList.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.list_item,null);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.book_icon);
                holder.textView = (TextView) convertView.findViewById(R.id.book_name);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.imageView.setVisibility(View.VISIBLE);
            holder.textView.setText(bookList.get(position));
            return convertView;
        }

    }
    private class ViewHolder{
        private ImageView imageView;
        private TextView textView;
    }
    @Override
    public void onResume(){
        super.onResume();
        bookList = iReaderDB.getBookName();
        MainPageAdapter adapter  = new MainPageAdapter(this);
        listView.setAdapter(adapter);
    }


}
