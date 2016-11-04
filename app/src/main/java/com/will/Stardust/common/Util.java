package com.will.Stardust.common;

import android.widget.Toast;

import com.will.Stardust.base.MyApplication;

/**
 * Created by will on 2016/10/29.
 */

public class Util {
    private static Toast mToast;
    public static void makeToast(String message){
        if(mToast == null){
            mToast = Toast.makeText(MyApplication.getGlobalContext(),"",Toast.LENGTH_SHORT);
        }
        mToast.setText(message);
        mToast.show();
    }
}
