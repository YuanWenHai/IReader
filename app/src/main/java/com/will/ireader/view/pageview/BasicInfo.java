package com.will.ireader.view.pageview;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by will on 2018/1/25.
 */

public class BasicInfo {
    private SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss", Locale.CHINA);
    private IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    public String getCurrentTime(){
        return formatter.format(Calendar.getInstance().getTime());
    }

    public int getCurrentBatteryState(Context context){
        Intent batteryStatus = context.registerReceiver(null, filter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (int)(level * 100 / (float)scale);
    }
}
