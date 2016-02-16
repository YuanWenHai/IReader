package com.will.Stardust.View;

import android.content.Context;
import android.widget.SimpleAdapter;

import com.will.Stardust.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Will on 2016/2/3.
 */
public class MenuAdapter {
    private String[] nameList = {"章节目录","进度跳转","字体大小","背景设置","夜间模式","全文查找",/*"字体颜色""调整行距","乱码按我"*/};
    private int [] iconList = {R.drawable.menu_directory, R.drawable.menu_progress,R.drawable.menu_font,R.drawable.menu_background,
    R.drawable.night_mode,R.drawable.menu_search,R.drawable.menu_search,/*R.drawable.menu_code*/};
    private  ArrayList<HashMap<String,Object>> mapList;
    SimpleAdapter adapter;
    public MenuAdapter(){

    }
    public  SimpleAdapter getMenuAdapter(Context context){
        mapList = new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> map;
        for(int i = 0;i<nameList.length;i++){
            map = new HashMap<String,Object>();
            map.put("name",nameList[i]);
            map.put("icon",iconList[i]);
            mapList.add(map);
        }
       adapter = new SimpleAdapter(context,mapList,R.layout.menu_item,new String[]{"name","icon"},
               new int[]{R.id.menu_text,R.id.menu_icon});
        return adapter;

    }
    public List<Map<String,Object>> getListData(){
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        Map<String,Object> map;
        for(int i = 0;i<nameList.length;i++){
            map = new HashMap<String,Object>();
            map.put("name",nameList[i]);
            map.put("icon",iconList[i]);
            list.add(map);
        }
        return list;

    }
}
