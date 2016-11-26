package com.will.ireader.page;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.will.ireader.R;
import com.will.ireader.view.PageView;
import com.will.ireader.base.BaseActivity;
import com.will.ireader.bean.Book;
import com.will.ireader.chapter.ChapterActivity;
import com.will.ireader.common.SPHelper;

/**
 * Created by will on 2016/11/3.
 */

public class PageActivity extends BaseActivity implements Animation.AnimationListener{
    private static final int REQUEST_CODE = 666;
    private PageFactory mPageFactory;
    private PageView pageView;
    private View actionBar;
    private Toolbar toolbar;
    private View statusBar;
    private TextView progressText;
    private BottomSheetBehavior<CardView> bottomSheetBehavior;
    private boolean isAnimating;
    private boolean isActionBarHidden = true;
    private int originPosition = -1;
    private boolean originMode;
    private boolean isNightMode = originMode = SPHelper.getInstance().isNightMode();
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        setTheme(isNightMode ? R.style.AppNightTheme : R.style.AppDayTheme);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.reading_activity_layout);
        Book book = (Book) getIntent().getSerializableExtra("book");

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
                finishReadingActivity();
            }
        });

        pageView.setSystemUiVisibility(View.INVISIBLE);
        mPageFactory = PageFactory.getInstance(pageView,book);
        mPageFactory.nextPage();

        pageView.setOnClickCallback(new PageView.OnClickCallback() {
            @Override
            public void onLeftClick() {
               if(!hadOtherWidgetShown()){
                   mPageFactory.prePage();
               }
            }

            @Override
            public void onMiddleClick() {
               if(!hadOtherWidgetShown()){
                   changeActionState();
               }
            }

            @Override
            public void onRightClick() {
                if(!hadOtherWidgetShown()){
                    mPageFactory.nextPage();
                }
            }
        });
        pageView.setOnScrollListener(new PageView.OnScrollListener() {
            @Override
            public void onLeftScroll() {
                if(!hadOtherWidgetShown()){
                    mPageFactory.nextPage();
                }
            }

            @Override
            public void onRightScroll() {
                if(!hadOtherWidgetShown()){
                    mPageFactory.prePage();
                }
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

    private boolean hadOtherWidgetShown(){
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
        return false;
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
            finishReadingActivity();
        }
    }

    private void finishReadingActivity(){
        PageFactory.close();
        if(originMode != SPHelper.getInstance().isNightMode()){
            setResult(RESULT_OK);
        }
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reading_menu,menu);
        //setNightMode(SPHelper.getInstance().isNightMode());
        iniTheme();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.page_menu_night_mode:
                setNightMode(!SPHelper.getInstance().isNightMode());
                break;
            case R.id.page_menu_chapter:
                final Intent intent = new Intent(this, ChapterActivity.class);
                changeActionState();
                if(!isAnimating){
                    pageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivityForResult(intent,REQUEST_CODE);
                        }
                    },300);
                }
                break;
            case R.id.page_menu_overflow:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                changeActionState();
                break;
        }

        return true;
    }

    private void iniTheme(){
        if(isNightMode){
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_brightness_6_white_24dp);
        }else{
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_brightness_7_white_24dp);
        }
    }
    private static final String FONT_STR = "当前字号：";
    private FloatingActionButton increaseFont;
    private FloatingActionButton decreaseFont;
    private View divider;
    private SeekBar progressBar;
    private CardView bottomSheet;

    private  void setNightMode(boolean which){
        if(which){
            //change toolbar
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_brightness_6_white_24dp);
            mPageFactory.setNightMode(true);
            animateViewColorChanging(getResources().getColor(R.color.colorPrimary),
                    getResources().getColor(R.color.nightColorPrimary),300,toolbar);
            animateViewColorChanging(getResources().getColor(R.color.colorPrimaryDark),
                    getResources().getColor(R.color.nightColorPrimaryDark),300,statusBar);
            //change bottom sheet
            increaseFont.setBackgroundTintList(getResources().getColorStateList(R.color.nightColorPrimaryLight));
            decreaseFont.setBackgroundTintList(getResources().getColorStateList(R.color.nightColorPrimaryLight));
            divider.setBackgroundColor(getResources().getColor(R.color.nightColorPrimaryLight));
            bottomSheet.setCardBackgroundColor(getResources().getColor(R.color.nightColorPrimary));
            changeSeekbarColor(progressBar,getResources().getColor(R.color.nightColorPrimaryLight));

            SPHelper.getInstance().setNightMode(true);
        }else{
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_brightness_7_white_24dp);
            mPageFactory.setNightMode(false);
            animateViewColorChanging(getResources().getColor(R.color.nightColorPrimary),
                    getResources().getColor(R.color.colorPrimary),300,toolbar);
            animateViewColorChanging(getResources().getColor(R.color.nightColorPrimaryDark),
                    getResources().getColor(R.color.colorPrimaryDark),300,statusBar);
            //as above
            increaseFont.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryLight));
            decreaseFont.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryLight));
            divider.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
            bottomSheet.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            changeSeekbarColor(progressBar,getResources().getColor(R.color.colorPrimaryLight));

            SPHelper.getInstance().setNightMode(false);
        }
    }
    private void animateViewColorChanging(int fromColor, int toColor, int duration, final View... views){
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(),fromColor,toColor);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for(View view : views){
                    view.setBackgroundColor((int)animation.getAnimatedValue());
                }
            }
        });
        animator.start();
    }
    private void iniBottomSheetMenu(){
        bottomSheet = (CardView) findViewById(R.id.reading_activity_bottom_sheet);
        divider = findViewById(R.id.reading_activity_divider);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        //change font
        increaseFont = (FloatingActionButton) findViewById(R.id.reading_activity_increase_font);
        decreaseFont = (FloatingActionButton) findViewById(R.id.reading_activity_decrease_font);
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
        progressBar = (SeekBar) findViewById(R.id.reading_activity_seek_bar);
        progressText = (TextView) findViewById(R.id.reading_activity_progress_text);
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
        changeSeekbarColor(progressBar,getResources().getColor(isNightMode? R.color.nightColorPrimaryLight : R.color.colorPrimaryLight));
    }

    public void changeSeekbarColor(SeekBar s,int color)
    {
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;

        LayerDrawable layerDrawable = (LayerDrawable) s.getProgressDrawable();
        Drawable progress =  layerDrawable.findDrawableByLayerId(android.R.id.progress);
        Drawable secondary =  layerDrawable.findDrawableByLayerId(android.R.id.secondaryProgress);
        Drawable background =  layerDrawable.findDrawableByLayerId(android.R.id.background);
        Drawable th = s.getThumb();

        // Setting colors
        progress.setColorFilter(color,mMode);
        secondary.setColorFilter(color,mMode);
        background.setColorFilter(color, mMode);
        th.setColorFilter(color,mMode);

        // Applying Tinted Drawables
        layerDrawable.setDrawableByLayerId(android.R.id.progress, progress);

        layerDrawable.setDrawableByLayerId(android.R.id.secondaryProgress, secondary);

        layerDrawable.setDrawableByLayerId(android.R.id.background, background);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(REQUEST_CODE == requestCode && resultCode == RESULT_OK && data!=null){
           PageFactory.getInstance().setPosition(data.getIntExtra("position",1));
           //跳转章节后进度也会变化，在此处更新进度值
           int progress = PageFactory.getInstance().getProgress();
           progressText.setText("当前进度："+progress+"%");
           progressBar.setProgress(progress);
       }
    }
}
