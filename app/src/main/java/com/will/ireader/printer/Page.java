package com.will.ireader.printer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.will.ireader.common.Util;

import java.util.Calendar;
import java.util.Locale;

/**
 * created  by will on 2019/5/11 16:54
 */
public class Page extends View {

    private String[] pageContent;
    private int contentWidth;
    private int contentHeight;

    private PageConfig mConfig = new PageConfig();
    private Paint paint;
    private Printer printer;

    public Page(Context context) {
        super(context);
        initialize();
    }

    public Page(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Page(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Page(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize() {
        Book b = new Book("test.txt","/storage/emulated/0/test.txt");
        printer = new Printer(b);


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(px(mConfig.getFontSize()));
    }

    private int px(int dp){
        return Util.getPXFromDP(dp);
    }

    private void initializePageSpec(){
        contentWidth = getMeasuredWidth() - px(mConfig.getContentPaddingHorizontal() * 2);
        contentHeight = getMeasuredHeight() - px(mConfig.getInfoPanelHeight() + 2*mConfig.getContentPaddingVertical());
    }

    private void pageDown() {
        pageContent = printer.printPageForward(contentWidth,contentHeight,paint);
        invalidate();
    }
    private void pageUp() {
        pageContent = printer.printPageBackward(contentWidth,contentHeight,paint);
        invalidate();
    }

    private void drawPage(Canvas canvas){
        if(pageContent == null){
            pageDown();
            return;
        }
        for(int i = 0; i<pageContent.length; i++){
            canvas.drawText(pageContent[i],px(mConfig.getContentPaddingHorizontal()),px(mConfig.getFontSize()) * (i+1),paint);
        }


        //info panel
        String timeInfo = Calendar.getInstance().getTime().toString();

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getContext().registerReceiver(null, filter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = level /scale;
        String butteryInfo = batteryPct+"%";

        String progressInfo = String.format(Locale.CHINA,"%.2f",printer.getProgress());



        // TODO: 2019/7/8  implement info panel.

    }

    @Override
    protected void onDraw(Canvas canvas) {
       drawPage(canvas);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initializePageSpec();
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



    public class PageConfig {
        private int fontSize = 16;
        //private int fontSpacing = 2;
        private int contentPaddingHorizontal = 2;
        private int contentPaddingVertical = 2;
        private int lineSpacing = 1;

        private int infoPanelHeight = 16;
        private int infoFontSize = 14;
        private int infoPaddingHorizontal = 2;
        private int infoPaddingVertical = 2;

        public int getInfoPanelHeight() {
            return infoPanelHeight;
        }

        public void setInfoPanelHeight(int infoPanelHeight) {
            this.infoPanelHeight = infoPanelHeight;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
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

        public int getInfoFontSize() {
            return infoFontSize;
        }

        public void setInfoFontSize(int infoFontSize) {
            this.infoFontSize = infoFontSize;
        }
    }
}
