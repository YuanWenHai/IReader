package com.will.ireader.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.will.ireader.R;
import com.will.ireader.View.ColorAdapter;
import com.will.ireader.file.FileSelector;
import com.will.ireader.file.IReaderDB;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Will on 2016/1/29.
 */
public class MainPageActivity extends Activity {
    private List<String> bookList;
    private List<String> pathList;
    private IReaderDB iReaderDB;
    private GridView listView;
    private Button add;
    private CheckBox checkBox;
    private RelativeLayout mainPageLayout;
    private Button setBackground;
    private final int GET_IMAGE = 1;
    private SharedPreferences sp;
    private AlertDialog customDialog;
    private Button customButton;
    private GridView customGridView;
    private  String backgroundImage = "";
    int colorNumber;
    MainPageAdapter adapter;
    private final int[] color = new int[]{Color.WHITE,Color.RED,Color.LTGRAY,Color.BLACK,Color.BLUE,
            Color.CYAN, Color.DKGRAY,Color.GRAY};

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        backgroundImage = getFilesDir().getPath()+"/"+"image";
        if(!(new File(backgroundImage).exists())){
            initializeBackgroundImage();
        }
        sp = getSharedPreferences("config",MODE_PRIVATE);
        colorNumber = sp.getInt("main_page_font_color", 0);
        setContentView(R.layout.main_page);
        mainPageLayout = (RelativeLayout) findViewById(R.id.main_page_layout);
        String filePath  = sp.getString("background_image","");
        if(!filePath.equals("")){
            Log.e("setBackground",filePath);
            mainPageLayout.setBackground(new BitmapDrawable(this.getResources(), filePath));
        }else{
            mainPageLayout.setBackground(new BitmapDrawable(this.getResources(),backgroundImage));
        }
        setBackground = (Button) findViewById(R.id.set_background_button);
        iReaderDB = IReaderDB.getInstance(this);
        bookList = iReaderDB.getBookName();
        pathList = iReaderDB.getBookPath();
        add = (Button) findViewById(R.id.add_button);
        add.setTextColor(color[colorNumber]);
        setBackground.setTextColor(color[colorNumber]);
        listView = (GridView)findViewById(R.id.list_view);
        adapter = new MainPageAdapter(this);
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
                LayoutInflater inflater = getLayoutInflater().from(MainPageActivity.this);
                View dialogView = inflater.inflate(R.layout.delete_dialog, null);
                TextView textView = (TextView) dialogView.findViewById(R.id.dialog_content);
                textView.setText("确定删除" + bookList.get(position) + "?");
                Button confirmDelete = (Button) dialogView.findViewById(R.id.confirm_delete);
                dialog.setView(dialogView);
                checkBox = (CheckBox) dialogView.findViewById(R.id.dialog_check_box);
                final AlertDialog deleteDialog = dialog.create();
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 200, 200, 200)));
                deleteDialog.show();

                confirmDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                        deleteDialog.cancel();
                    }
                });

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
                initializeCustomDialog();
                customDialog.show();
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
            holder.textView.setText(bookList.get(position).replace(".txt",""));
            holder.textView.setTextColor(color[colorNumber]);
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
    private void initializeCustomDialog(){
        if(customDialog ==null){
            customDialog = new AlertDialog.Builder(this).create();
            View view = View.inflate(this, R.layout.main_page_custom, null);
            customButton = (Button) view.findViewById(R.id.main_page_custom_button);
            customGridView = (GridView) view.findViewById(R.id.main_page_custom_grid_view);
            customGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            customGridView.setAdapter(new ColorAdapter(this));
            customDialog.setView(view);
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.argb(100, 200, 200, 200)));
            customGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    colorNumber = position;
                    listView.setAdapter(new MainPageAdapter(MainPageActivity.this));
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("main_page_font_color", colorNumber);
                    editor.commit();
                    setBackground.setTextColor(color[colorNumber]);
                    add.setTextColor(color[colorNumber]);
                }
            });
            customButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GET_IMAGE);
                }
            });
        }
    }
    private void initializeBackgroundImage(){
        AssetManager manager = getAssets();
        String fileDir = getFilesDir().getPath();
        File dir = new File(fileDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        try{
            InputStream is = manager.open("main_background.jpg");
            BufferedInputStream bi = new BufferedInputStream(is);
            BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(dir+"/"+"image"));
            byte[] temp = new byte[1024];
            int length = 0;
            while((length =bi.read(temp)) > 0) {
                bo.write(temp);
            }
            bi.close();
            bo.close();
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
}
