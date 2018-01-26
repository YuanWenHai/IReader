package com.will.ireader.view.pageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.will.ireader.common.Util;
import com.will.ireader.page.PageInfo;

/**
 * 一个用于展示书籍阅读界面的自定义View.<br/>
 * Created by Will on 2016/2/2.
 */
public class PageView extends View {

    private int mScreenWidth;
    private int mScreenHeight;

    private Paint textPaint;
    private Paint bottomBarPaint;

    private PageInfo pageInfo;

    private int currentPosition = -1;

    private BasicInfo basicInfo = new BasicInfo();


    public PageView(Context context){
        super(context);
    }
    public PageView(Context context, AttributeSet attr){
        super(context,attr);
    }

    public void setFontSize(int size){
        size = Math.min(pageInfo.getFontMaxSize(),Math.max(pageInfo.getFontMinSize(),size));
        if(pageInfo.getFontSize() != size){
            pageInfo.setFontSize(size);
            invalidate();
        }
    }
    public int getFontSize(){
        return pageInfo.getFontSize();
    }

    public void setProgress(int progress){
        progress = Math.max(0,Math.min(100,progress));
        int pos = PageInfo.bookStr.length()/progress*100;
        pageInfo.setNextStartPosition(pos);
        invalidate();
    }
    public int getProgress(){
        return (pageInfo.getNextStartPosition()-1)*100/PageInfo.bookStr.length();
    }

    public void setNightMode(boolean which){

    }
    public void saveCurrentPosition(){
        currentPosition = pageInfo.getStartPosition();
    }
    public void restorePosition(){
        if(currentPosition != -1){
            return;
        }
        pageInfo.setNextStartPosition(currentPosition);
        currentPosition  =  -1;
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.save();
        print(canvas);
        //canvas.drawColor(Color.BLUE);
        //canvas.drawBitmap(bit,0,0,null);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mScreenHeight = getMeasuredHeight();
        mScreenWidth = getMeasuredWidth();
    }

    private void print(Canvas canvas){
        int rowCount = measureTextRows(pageInfo.getContentTopOffset()+pageInfo.getContentBottomOffset()+pageInfo.getBottomBarSize());
        //draw content
        int textCount = 0;
        for(int i=1;i<=rowCount;i++){
            String nextRow = moveToNextRow(textCount,pageInfo.getRowStartOffset()+pageInfo.getRowEndOffset());
            textCount += nextRow.length();
            canvas.drawText(nextRow,
                    pageInfo.getRowStartOffset(),pageInfo.getFontSize()*i+pageInfo.getContentTopOffset(),textPaint);
        }
        //draw bottom bar
        drawBottomBar(canvas);
    }
    public void setPageInfo(PageInfo pageInfo){
        this.pageInfo = pageInfo;
        initializeParams();
    }
    public void pageDown(){
        pageInfo.setStartPosition(pageInfo.getNextStartPosition());
        invalidate();
    }
    public void pageUp(){

    }

    private void initializeParams(){
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(pageInfo.getFontSize());
        bottomBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottomBarPaint.setTextSize(pageInfo.getBottomBarFontSize());
    }


    /**
     * 获取下一行内容，并移动nextStartPosition.<br/>
     * @param startPos 开始读取的位置
     * @param offset 行偏移量，用于测算每一行的字数
     * @return 行内容
     */
    private String moveToNextRow(int startPos,int offset){
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for(;;){
            int nextPos = startPos + i;
            if(nextPos >= PageInfo.bookStr.length()){
                pageInfo.setNextStartPosition(nextPos);
                return builder.toString();
            }
            char nextChar = PageInfo.bookStr.charAt(nextPos);
            if(nextChar == '\n'){
                builder.append(nextChar);
                pageInfo.setNextStartPosition(nextPos+1);
                return builder.toString();
            }

            builder.append(nextChar);
            if(textPaint.measureText(builder.toString()) < (mScreenWidth - offset)){
                i++;
            }else{
                pageInfo.setNextStartPosition(nextPos);
                return builder.deleteCharAt(i).toString();
            }
        }
    }
    private int getLastRowStartPos(int startPos,int offset){
        int i = 0;//因为startPos的位置是当前行的行首，故这里i初始值为1是为将其移动到上一行末尾
        for(;;){
            int lastPos = startPos - i;
            if(lastPos <= 0){
                return 0;
            }
            char lastChar = PageInfo.bookStr.charAt(lastPos);
            if(lastChar == '\n' && i != 1){
                return lastPos;
            }
            if(textPaint.measureText(builder.toString()) < (mScreenWidth - offset)){
                i++;
            }else{
                pageInfo.setNextStartPosition(lastPos);
                return builder.deleteCharAt(i).toString();
            }
        }
    }

    /**
     * 获取页面行数
     * @param offset y offset
     * @return 行数
     */
    private int measureTextRows(int offset){
        return (mScreenHeight-offset)/pageInfo.getFontSize();
    }

    private void drawBottomBar(Canvas canvas){
        int y = mScreenHeight - ((pageInfo.getBottomBarSize()-pageInfo.getBottomBarFontSize())/2);
        int startOffset = pageInfo.getBottomBarStartOffset();
        int endOffset = pageInfo.getBottomBarEndOffset();
        //绘制时间
        canvas.drawText(basicInfo.getCurrentTime(),startOffset,y,bottomBarPaint);
        //绘制进度
        String percentage = String.valueOf(getProgress())+"%";
        int percentageTextLength = (int) bottomBarPaint.measureText(percentage);
        canvas.drawText(percentage,(mScreenWidth-startOffset-endOffset-percentageTextLength)/2,y,bottomBarPaint);
        //绘制电量
        String batteryState = "电量:"+String.valueOf(basicInfo.getCurrentBatteryState(getContext()));
        int batteryStateTextLength = (int) bottomBarPaint.measureText(batteryState);
        canvas.drawText(batteryState,mScreenWidth-endOffset-batteryStateTextLength,y,bottomBarPaint);
    }


    @Override
    public boolean isInEditMode() {
        return true;
    }
}
