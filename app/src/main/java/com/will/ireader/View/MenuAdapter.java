package com.will.ireader.View;

import android.content.Context;
import android.widget.SimpleAdapter;

import com.will.ireader.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Will on 2016/2/3.
 */
public class MenuAdapter {
    private String[] nameList = {"章节目录","进度跳转","字体大小","背景设置","夜间模式","调整行距","全文查找","乱码按我"};
    private int [] iconList = {R.drawable.menu_directory, R.drawable.menu_progress,R.drawable.menu_font,R.drawable.menu_background,
    R.drawable.menu_night_mode,R.drawable.menu_line_space,R.drawable.menu_search,R.drawable.menu_code};
    private  ArrayList<Map<String,Object>> mapList;
    public MenuAdapter(){
        mapList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();
        for(int i = 0;i<nameList.length;i++){
            map.put("name",nameList[i]);
            map.put("icon",iconList[i]);
            mapList.add(map);
        }
    }
    public  SimpleAdapter getMenuAdapter(Context context){
       SimpleAdapter adapter = new SimpleAdapter(context,mapList,R.layout.menu_item,new String[]{"name","icon"},
               new int[]{R.id.menu_text,R.id.menu_icon});
        return adapter;

    }
}
