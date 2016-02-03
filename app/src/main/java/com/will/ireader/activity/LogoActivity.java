package com.will.ireader.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.will.ireader.R;


/**
 * Created by Will on 2016/1/29.
 */
public class LogoActivity extends Activity {
    private LinearLayout logo;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo);
        logo = (LinearLayout) findViewById(R.id.logo);
        AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
        animation.setDuration(1500);
        logo.startAnimation(animation);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1500);
                Intent intent = new Intent(LogoActivity.this, MainPageActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();

    }


}
