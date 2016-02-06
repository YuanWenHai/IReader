package com.will.ireader.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TextView;

import com.will.ireader.R;
import com.will.ireader.activity.MainPageActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Will on 2016/1/31.
 */
public class FileSelector extends Activity {
    private List<Map<String,Object>> dataList;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private ListView listView;
    private Button confirm;
    private Button cancel;
    private Boolean chooseFolder = false;
    private Map<Integer,Boolean> checkStatus;
    private IReaderDB iReaderDB;
    int currentPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.file_selector_view);
        getData();
        iReaderDB = IReaderDB.getInstance(this);
        listView = (ListView) findViewById(R.id.file_selector_list);
        confirm = (Button) findViewById(R.id.file_selector_confirm);
        cancel = (Button) findViewById(R.id.file_selector_cancel);
        FileListAdapter fileListAdapter = new FileListAdapter(this);
        listView.setAdapter(fileListAdapter);
        //ListView选项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
        public void onItemClick(AdapterView<?>arg0,View view,int position,long id){
                currentPosition = position;
                if(chooseFolder){
                    //应将点击事件设为设置check状态
                } else if((Integer)dataList.get(position).get("icon")==R.drawable.icon_folder){
                    currentPath = (String) dataList.get(position).get("path");
                    getData();
                    FileListAdapter adapter = new FileListAdapter(FileSelector.this);
                    listView.setAdapter(adapter);
                }else{
                    //将文件名与路径写入SQLiteDatabase中,过滤非txt文件
                    if(((String)dataList.get(position).get("name")).toUpperCase().contains(".TXT")){
                    iReaderDB.saveBook((String)dataList.get(position).get("name"),(String)dataList.get(position).get("path"));
                    Log.e("OnItemClickListener",(String)dataList.get(position).get("path"));
                }}
            }
        });
        //listview长按事件，转换到多选checkBox模式
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
        public boolean onItemLongClick(AdapterView<?> arg0,View view,int position,long id){
                currentPosition = position;
                chooseFolder = true;
                FileListAdapter adapter = new FileListAdapter(FileSelector.this);
                listView.setAdapter(adapter);
                listView.setSelection(currentPosition);
                return true;
            }
        });
        //确定键点击事件
        confirm.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View view ){
                for(int i =0;i<checkStatus.size();i++){
                    //以checkBox的选定状况为判定条件，通过在getData中为Dir与File设定的不同图标，判定其类别，并存入不同的表格
                    if(checkStatus.get(i)){
                        if((Integer)dataList.get(i).get("icon")==R.drawable.icon_folder) {
                            String dirName = (String) dataList.get(i).get("name");
                            String dirPath = (String) dataList.get(i).get("path");
                            iReaderDB.saveDir(dirName, dirPath);
                            //只添加.txt文件
                        }else if(((String)dataList.get(i).get("name")).toUpperCase().contains(".TXT")){
                            String bookName = (String) dataList.get(i).get("name");
                            String bookPath = (String) dataList.get(i).get("path");
                            iReaderDB.saveBook(bookName,bookPath);
                        }
                    }
                }
                //新开一线程进行文件夹扫描，将txt文件加入book列表
                new Thread (new Runnable(){
                    @Override
                public void run (){


                            File file;


                                List<String> list = iReaderDB.getDirPath();
                                if(list != null){
                                for(int i =0;i<list.size();i++){
                                    file = new File(list.get(i));
                                    File[] files = file.listFiles();
                                    if(files.length != 0){
                                    recursion(files);
                                    }
                                }
                                }
                            }
                            //递归得到根目录并将txt文件目录与名字加入表格；
                            private void recursion(File[] files ){
                                if(!(files.length==0)){
                                    for(int i = 0;i<files.length;i++){
                                        if( files[i].isDirectory()){
                                            recursion(files[i].listFiles());
                                        }else if (files[i].getName().toUpperCase().contains(".TXT")){
                                            iReaderDB.saveBook(files[i].getName(),files[i].getPath());
                                            Intent intent = new Intent (FileSelector.this,MainPageActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            }



                }).start();

            }
        });
        //取消键
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View view){
                Intent intent = new Intent(FileSelector.this, MainPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    //得到currentPath的子项，将名字、路径、图片存入map文件中（dataList）
    private void getData(){
        File file = new File(currentPath);
        List<Map<String,Object>> list  = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = null;
        File[] files = file.listFiles();
        for(int i = 0;i<files.length;i++){
            map = new HashMap<String,Object>();
            map.put("name",files[i].getName());
            map.put("path",files[i].getPath());
            if(files[i].isDirectory()){
                map.put("icon",R.drawable.icon_folder);
            }else{
                map.put("icon",R.drawable.icon_doc);
            }
            list.add(map);
        }
        dataList = list;
    }
    //适配器
    class FileListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        FileListAdapter(Context context){
            this.inflater = LayoutInflater.from(context);
            //初始化checkBox点击事件记录器
            checkStatus = new HashMap<Integer,Boolean>();
            for(int i = 0;i<dataList.size();i++){
                checkStatus.put(i,false);
            }
        }
        @Override
        public int getCount(){
            return dataList.size();
        }
        @Override
        public long getItemId(int arg0){
            return 0;
        }
        @Override
        public Object getItem(int arg0){
            return null;
        }
        public View getView(final int position,View convertView,ViewGroup parent){
            ViewHolder holder = null;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.file_selector_item,null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.file_name);
                holder.imageView = (ImageView) convertView.findViewById(R.id.file_icon);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
                holder.checkBox.setVisibility(View.INVISIBLE);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText((String) dataList.get(position).get("name"));
            holder.imageView.setImageResource((Integer)dataList.get(position).get("icon"));
            //长按--多选开关
            if(chooseFolder){
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setOnClickListener(new View.OnClickListener(){
                    @Override
                    //通过click事件，修改checkBox状态记录器内容
                public void onClick(View v){
                        if(checkStatus.get(position)){
                            checkStatus.put(position,false);
                        }else{
                            checkStatus.put(position,true);
                        }
                    }
                });

            }
            return convertView;
        }
    }
    class ViewHolder{
        public CheckBox checkBox;
        public TextView textView;
        public ImageView imageView;
    }
    @Override
    public void onBackPressed(){
        //如果当前是多选模式，back返回默认模式
        if(chooseFolder){
            chooseFolder = false;
            FileListAdapter adapter = new FileListAdapter(this);
            listView.setAdapter(adapter);
            //如果当前是根目录，back正常返回MainPage
        }else if(currentPath.equals(rootPath)){
            super.onBackPressed();
            //否则，即是处于子目录层，通过currentPath得到父目录，重新getData，刷新当前界面
        }else{
            File file = new File(currentPath);
            currentPath = file.getParent();
            getData();
            Log.e("onBackPressed",currentPosition+"");
            FileListAdapter adapter = new FileListAdapter(FileSelector.this);
            listView.setAdapter(adapter);
            listView.setSelection(currentPosition);

        }
    }



}
