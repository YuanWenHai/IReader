package com.will.ireader.printer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.will.ireader.R;
import com.will.ireader.base.BaseActivity;

/**
 * created  by will on 2019/7/5 15:43
 */
public class PageActivity1 extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppNightTheme);
        setContentView(new Page(this));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
