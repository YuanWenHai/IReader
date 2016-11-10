package com.will.Stardust;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.will.Stardust.View.PageView;
import com.will.Stardust.base.BaseActivity;
import com.will.Stardust.bean.Book;
import com.will.Stardust.chapter.ChapterActivity;
import com.will.Stardust.common.SPHelper;

/**
 * Created by will on 2016/11/3.
 */

public class ReadingActivity extends BaseActivity implements Animation.AnimationListener{
    private static final int REQUEST_CODE = 666;
    private PageFactory mPageFactory;
    private PageView pageView;
    private View actionBar;
    private Toolbar toolbar;
    private View statusBar;
    private BottomSheetBehavior<CardView> bottomSheetBehavior;
    private boolean isAnimating;
    private boolean isActionBarHidden = true;
    private int originPosition = -1;
    private Book book;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.reading_activity_layout);
        book = (Book) getIntent().getSerializableExtra("book");

        pageView = (PageView) findViewById(R.id.reading_activity_view);
        actionBar = findViewById(R.id.reading_activity_action_bar);
        statusBar = findViewById(R.id.reading_activity_status_bar);
        toolbar = (Toolbar)findViewById(R.id.reading_activity_toolbar);
        toolbar.setTitle(book.getBookName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        pageView.setSystemUiVisibility(View.INVISIBLE);
        mPageFactory = PageFactory.getInstance(pageView,book);
        mPageFactory.nextPage();


        pageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(isAnimating){
                        return true;
                    }
                    if(!isActionBarHidden){
                        changeActionState();
                        return true;
                    }
                    if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        originPosition = -1;
                        return true;
                    }
                    if(motionEvent.getX() > pageView.getWidth()* 0.66f){
                        mPageFactory.nextPage();
                    }else if(motionEvent.getX() < pageView.getWidth()*0.33f){
                        mPageFactory.prePage();
                    }else{
                      changeActionState();
                    }
                }
                return true;
            }
        });
        iniBottomSheetMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPageFactory.saveBookmark();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isActionBarHidden){
            pageView.setSystemUiVisibility(View.INVISIBLE);
        }
    }

    private void changeActionState(){
        if(isActionBarHidden){
            actionBar.setVisibility(View.VISIBLE);
            pageView.setSystemUiVisibility(View.VISIBLE);
            TranslateAnimation animation = new TranslateAnimation(0,0,-actionBar.getHeight(),0);
            animation.setDuration(300);
            animation.setFillAfter(true);
            animation.setAnimationListener(this);
            actionBar.startAnimation(animation);
        }else{
            pageView.setSystemUiVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(0,0,0,-actionBar.getHeight());
            animation.setDuration(300);
            animation.setFillAfter(true);
            animation.setAnimationListener(this);
            actionBar.startAnimation(animation);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
        isAnimating = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        isAnimating = false;
        isActionBarHidden = !isActionBarHidden;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onBackPressed() {
        if(!isActionBarHidden){
           changeActionState();
        } else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            originPosition = -1;
        } else{
            PageFactory.close();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_toolbar_menu,menu);
        setNightMode(SPHelper.getInstance().isNightMode());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.page_menu_night_mode:
                setNightMode(!SPHelper.getInstance().isNightMode());
                break;
            case R.id.page_menu_chapter:
                Intent intent = new Intent(this, ChapterActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
                break;
            case R.id.page_menu_overflow:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                changeActionState();
                break;
        }

        return true;
    }
    private  void setNightMode(boolean which){
        if(which){
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_brightness_6_white_24dp);
            mPageFactory.setNightMode(true);
            toolbar.setBackgroundColor(getResources().getColor(R.color.blueGrey));
            statusBar.setBackgroundColor(getResources().getColor(R.color.blueGreyDark));
            SPHelper.getInstance().setNightMode(true);
        }else{
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_brightness_7_white_24dp);
            toolbar.setPopupTheme(R.style.ToolbarPopThemeLight);
            mPageFactory.setNightMode(false);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            statusBar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            SPHelper.getInstance().setNightMode(false);
        }
    }
    private static final String FONT_STR = "当前字号：";
    private void iniBottomSheetMenu(){
        CardView bottomSheet = (CardView) findViewById(R.id.reading_activity_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        //change font
        FloatingActionButton increaseFont = (FloatingActionButton) findViewById(R.id.reading_activity_increase_font);
        FloatingActionButton decreaseFont = (FloatingActionButton) findViewById(R.id.reading_activity_decrease_font);
        final TextView fontText = (TextView) findViewById(R.id.reading_activity_font_text);
        fontText.setText((FONT_STR + mPageFactory.getFontSize()));
        increaseFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPageFactory.increaseFontSize();
                fontText.setText((FONT_STR + mPageFactory.getFontSize()));
            }
        });
        decreaseFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPageFactory.decreaseFontSize();
                fontText.setText((FONT_STR + mPageFactory.getFontSize()));
            }
        });
        //change progress
        final SeekBar progressBar = (SeekBar) findViewById(R.id.reading_activity_seek_bar);
        final TextView progressText = (TextView) findViewById(R.id.reading_activity_progress_text);
        final ImageView resetProgress = (ImageView) findViewById(R.id.reading_activity_progress_reset);
        progressBar.setProgress(mPageFactory.getProgress());
        progressText.setText("当前进度："+mPageFactory.getProgress()+"%");
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressText.setText("当前进度："+i+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = mPageFactory.setProgress(seekBar.getProgress());
                if(originPosition < 0){
                    originPosition = i;
                }
            }
        });
        resetProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(originPosition >= 0){
                   mPageFactory.setPosition(originPosition);
                   progressText.setText("当前进度："+mPageFactory.getProgress()+"%");
                   progressBar.setProgress(mPageFactory.getProgress());
               }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(REQUEST_CODE == requestCode && resultCode == RESULT_OK && data!=null){
           PageFactory.getInstance().setPosition(data.getIntExtra("position",1));
       }
    }
}
