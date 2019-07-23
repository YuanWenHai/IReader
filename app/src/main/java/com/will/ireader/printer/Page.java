package com.will.ireader.printer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.will.ireader.common.SPHelper;
import com.will.ireader.common.Util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * created  by will on 2019/5/11 16:54
 */
public class Page extends View {

    private String[] pageContent;
    private int contentWidth;
    private int contentHeight;

    private PageConfig mConfig;
    private Paint contentPaint;
    private Paint infoPanelPaint;
    private Printer printer;

    public Page(Context context) {
        super(context);
        initialize();
        initContentPaint();
    }

    public Page(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
        initContentPaint();
    }

    public Page(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Page(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PageConfig getConfig(){
        return mConfig;
    }

    public void setPrinter(Printer printer){
        this.printer = printer;
        invalidate();
    }

    private void initialize(){
        mConfig = PageConfig.getInstance();
        infoPanelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPanelPaint.setColor(mConfig.getInfoColor());
        infoPanelPaint.setTextSize(px(mConfig.getInfoFontSize()));
    }
    private void initContentPaint() {
        contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(Color.BLACK);
        contentPaint.setTextSize(px(mConfig.getFontSize()));
    }

    private int px(int dp){
        return Util.getPXFromDP(dp);
    }

    public void refresh(){
        initContentPaint();
        pageContent = printer.reprintCurrentPage(contentWidth,contentHeight, contentPaint);
        invalidate();
    }

    /**
     * calculate available spec
     */
    private void calculatePageSpec(){
        contentWidth = getMeasuredWidth() - px(mConfig.getContentPaddingHorizontal() * 2);
        contentHeight = getMeasuredHeight() - px(mConfig.getInfoFontSize() + 2*mConfig.getContentPaddingVertical());
    }

    private void pageDown() {
        pageContent = printer.printPageForward(contentWidth,contentHeight, contentPaint);
        invalidate();
    }
    private void pageUp() {
        pageContent = printer.printPageBackward(contentWidth,contentHeight, contentPaint);
        invalidate();
    }

    private void drawPage(Canvas canvas){
        if(printer == null){
            return;
        }
        if(pageContent == null){
            pageDown();
            return;
        }
        for(int i = 0; i<pageContent.length; i++){
            canvas.drawText(pageContent[i],px(mConfig.getContentPaddingHorizontal()),px(mConfig.getFontSize()) * (i+1), contentPaint);
        }


        //info panel
        String timeInfo = DateFormat.format("HH:mm",Calendar.getInstance().getTimeInMillis()).toString();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getContext().registerReceiver(null, filter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        String batteryInfo = String.valueOf((level /scale)*100);
        String progressInfo = String.format(Locale.CHINA,"%.2f%%",printer.getProgress());

        int y = getMeasuredHeight()-px(mConfig.getInfoPaddingVertical());
        canvas.drawText(timeInfo,px(mConfig.getInfoPaddingHorizontal()),y,infoPanelPaint);
        canvas.drawText(progressInfo,(getMeasuredWidth()-infoPanelPaint.measureText(progressInfo))/2,y,infoPanelPaint);
        canvas.drawText(batteryInfo,getMeasuredWidth()-infoPanelPaint.measureText(batteryInfo)-mConfig.getInfoPaddingHorizontal(),y,infoPanelPaint);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPage(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculatePageSpec();
    }

    private float pressX = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            pressX = event.getX();

            return true;
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            if(pressX < getMeasuredWidth()/2){
                pageUp();
            }else{
                pageDown();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }



    public static class PageConfig {
        private int fontSize = 16;
        //private int fontSpacing = 2;
        private int contentPaddingHorizontal = 2;
        private int contentPaddingVertical = 2;
        private int lineSpacing = 1;

        private int infoFontSize = 16;
        private int infoPaddingHorizontal = 2;
        private int infoPaddingVertical = 8;
        private int infoColor = Color.BLACK;


        public int getInfoColor() {
            return infoColor;
        }

        public void setInfoColor(int infoColor) {
            this.infoColor = infoColor;
        }

        static PageConfig getInstance(){
            PageConfig config = new PageConfig();
            config.setFontSize(SPHelper.getInstance().getFontSize(16));
            return config;
        }



        public int getInfoFontSize() {
            return infoFontSize;
        }

        public void setInfoFontSize(int infoFontSize) {
            this.infoFontSize = infoFontSize;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
            SPHelper.getInstance().setFontSize(fontSize);
        }

        public int getLineSpacing() {
            return lineSpacing;
        }

        public void setLineSpacing(int lineSpacing) {
            this.lineSpacing = lineSpacing;
        }


        /*     public int getFontSpacing() {
            return fontSpacing;
        }

        public void setFontSpacing(int fontSpacing) {
            this.fontSpacing = fontSpacing;
        }*/

        public int getContentPaddingHorizontal() {
            return contentPaddingHorizontal;
        }

        public void setContentPaddingHorizontal(int contentPaddingHorizontal) {
            this.contentPaddingHorizontal = contentPaddingHorizontal;
        }

        public int getContentPaddingVertical() {
            return contentPaddingVertical;
        }

        public void setContentPaddingVertical(int contentPaddingVertical) {
            this.contentPaddingVertical = contentPaddingVertical;
        }

        public int getInfoPaddingHorizontal() {
            return infoPaddingHorizontal;
        }

        public void setInfoPaddingHorizontal(int infoPaddingHorizontal) {
            this.infoPaddingHorizontal = infoPaddingHorizontal;
        }

        public int getInfoPaddingVertical() {
            return infoPaddingVertical;
        }

        public void setInfoPaddingVertical(int infoPaddingVertical) {
            this.infoPaddingVertical = infoPaddingVertical;
        }

    }
}
