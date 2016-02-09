package com.will.ireader.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import android.widget.RelativeLayout;
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
    private List<String> bookList;
    private List<String> pathList;
    private IReaderDB iReaderDB;
    private ListView listView;
    private Button add;
    private CheckBox checkBox;
    private RelativeLayout mainPageLayout;
    private Button setBackground;
    private final int GET_IMAGE = 1;
    private SharedPreferences sp;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_page);
        mainPageLayout = (RelativeLayout) findViewById(R.id.main_page_layout);
        sp = getSharedPreferences("config",MODE_PRIVATE);
        String filePath  = sp.getString("background_image","");
        if(!filePath.equals("")){
            Log.e("setBackground",filePath);
            mainPageLayout.setBackground(new BitmapDrawable(this.getResources(), filePath));
        }
        setBackground = (Button) findViewById(R.id.set_background_button);
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
                Intent intent = new Intent(MainPageActivity.this, BookPage.class);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });
        setBackground.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View view ){
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,GET_IMAGE);
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
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            String filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("background_image", filePath);
            editor.commit();
            mainPageLayout.setBackground(new BitmapDrawable(this.getResources(), filePath));
        }

    }
}
