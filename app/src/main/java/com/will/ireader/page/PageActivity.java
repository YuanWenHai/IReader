package com.will.ireader.page;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
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
import com.will.ireader.view.pageview.PageTheme;
import com.will.ireader.view.pageview.PageView;
import com.will.ireader.base.BaseActivity;
import com.will.ireader.chapter.ChapterActivity;
import com.will.ireader.common.SPHelper;

/**
 * Created by will on 2016/11/3.
 */

public class PageActivity extends BaseActivity implements Animation.AnimationListener{
    private static final int REQUEST_CODE = 666;
    //private PageFactory mPageFactory;
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
    private PageTheme currentTheme = PageTheme.DAY_THEME;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        setTheme(isNightMode ? R.style.AppNightTheme : R.style.AppDayTheme);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.reading_activity_layout);
        initializeView();
        iniBottomSheetMenu();
        final PageTheme theme = SPHelper.getInstance().isNightMode() ? PageTheme.NIGHT_THEME : PageTheme.DAY_THEME;
        setTheme(theme);
        currentTheme = theme;
    }

    private void initializeView(){
        PageInfo pageInfo = (PageInfo) getIntent().getSerializableExtra(PageInfo.PAGE_INFO);
        pageView = findViewById(R.id.reading_activity_view);
        pageView.setPageInfo(pageInfo);
        pageView.setOnMenuClickListener(new PageView.OnMenuClickListener() {
            @Override
            public void onOptionMenuClick() {
                changeActionState();
            }
        });

        actionBar = findViewById(R.id.reading_activity_action_bar);
        statusBar = findViewById(R.id.reading_activity_status_bar);
        toolbar = findViewById(R.id.reading_activity_toolbar);
        toolbar.setTitle(pageInfo.getBook().getBookName());
        toolbar.setNavigationIcon(R.drawable.arrow_back_holo_dark_no_trim_no_padding);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishReadingActivity();
            }
        });

        pageView.setSystemUiVisibility(View.INVISIBLE);
    }
    @Override
    protected void onPause() {
        super.onPause();

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
        setupThemeIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.page_menu_night_mode:
                PageTheme toTheme = PageTheme.DAY_THEME.equals(currentTheme) ? PageTheme.NIGHT_THEME : PageTheme.DAY_THEME;
                setTheme(currentTheme,toTheme);
                currentTheme = toTheme;
                SPHelper.getInstance().setNightMode(toTheme.equals(PageTheme.NIGHT_THEME));
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
                pageView.saveCurrentPosition();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                changeActionState();
                break;
        }

        return true;
    }

    private void setupThemeIcon(){
        toolbar.getMenu().getItem(0).setIcon(currentTheme.getThemeIconRes());
    }
    private static final String FONT_STR = "当前字号：";
    private FloatingActionButton increaseFont;
    private FloatingActionButton decreaseFont;
    private View divider;
    private SeekBar progressBar;
    private CardView bottomSheet;


    private void setTheme(PageTheme theme){
        setTheme(null,theme);
    }
    private void setTheme(@Nullable PageTheme fromTheme, PageTheme toTheme){
        if(toTheme.equals(fromTheme)){
            return;
        }
        if(toolbar.getMenu().size() > 0){
            toolbar.getMenu().getItem(0).setIcon(toTheme.getThemeIconRes());
        }
        if(fromTheme == null){
            pageView.setPageTheme(toTheme);
        }else{
            changeViewThemeWithAnimation(fromTheme, toTheme,300,toolbar,pageView);
        }
        //change bottom sheet
        //increaseFont.setBackgroundTintList(getResources().getColorStateList(toTheme.getWidgetColor()));
        //decreaseFont.setBackgroundTintList(getResources().getColorStateList(toTheme.getWidgetColor()));
        divider.setBackgroundColor(toTheme.getWidgetColor());
        bottomSheet.setCardBackgroundColor(toTheme.getSheetColor());
        changeSeekbarColor(progressBar,toTheme.getWidgetColor());
    }

    private void changeViewThemeWithAnimation(final PageTheme fromTheme, final PageTheme toTheme, int duration, final Toolbar toolbar, final PageView pageView){
        ValueAnimator animator = ValueAnimator.ofInt(0,1);
        animator.setDuration(duration);
        final ArgbEvaluator evaluator = new ArgbEvaluator();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                int toolbarBackgroundColor = (int) evaluator.evaluate(fraction,fromTheme.getToolbarColor(),toTheme.getToolbarColor());
                toolbar.setBackgroundColor(toolbarBackgroundColor);

                int pageViewBackgroundColor = (int) evaluator.evaluate(fraction,fromTheme.getBackgroundColor(),toTheme.getBackgroundColor());
                pageView.setBackgroundColor(pageViewBackgroundColor);

                int pageViewFontColor = (int) evaluator.evaluate(fraction,fromTheme.getFontColor(),toTheme.getFontColor());
                pageView.setFontColor(pageViewFontColor);
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
        fontText.setText((FONT_STR + pageView.getFontSize()));
        increaseFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageView.setFontSize(pageView.getFontSize()+1);
                fontText.setText((FONT_STR + pageView.getFontSize()));
            }
        });
        decreaseFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageView.setFontSize(pageView.getFontSize()-1);
                fontText.setText((FONT_STR + pageView.getFontSize()));
            }
        });
        //change progress
        progressBar = (SeekBar) findViewById(R.id.reading_activity_seek_bar);
        progressText = (TextView) findViewById(R.id.reading_activity_progress_text);
        final ImageView resetProgress = (ImageView) findViewById(R.id.reading_activity_progress_reset);
        progressBar.setProgress(pageView.getProgress());
        progressText.setText("当前进度："+pageView.getProgress()+"%");
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
                pageView.setProgress(seekBar.getProgress());
                int i = pageView.getProgress();
                if(originPosition < 0){
                    originPosition = i;
                }
            }
        });
        resetProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageView.restorePosition();
                progressText.setText("当前进度："+pageView.getProgress()+"%");
                progressBar.setProgress(pageView.getProgress());
            }
        });
        changeSeekbarColor(progressBar,getResources().getColor(isNightMode? R.color.nightColorPrimaryLight : R.color.colorPrimaryLight));
    }

    private void changeSeekbarColor(SeekBar s,int color) {
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
