package com.will.ireader.view.pageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

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

    //这里之所以将这两个颜色参数单独列为全局变量而不直接使用PageInfo中颜色参数，是因为在Activity中需要多次调用setTextColor/setBackgroundColor方法，
    //而PageInfo中的setFontColor..之类方法有IO操作，频繁调用恐非明智之举。
    private int fontColor;
    private int backgroundColor;

    private PageInfo pageInfo;

    private int currentPosition = -1;

    private BasicInfo basicInfo = new BasicInfo();

    private float pressedX;
    private boolean moved;
    private int touchSlop;

    private OnMenuClickListener mListener;

    public PageView(Context context){
        super(context);
    }
    public PageView(Context context, AttributeSet attr){
        super(context,attr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setPageInfo(PageInfo pageInfo){
        this.pageInfo = pageInfo;
        initializeParams();
    }

    public void setFontSize(int size){
        size = Math.min(pageInfo.getFontMaxSize(),Math.max(pageInfo.getFontMinSize(),size));
        if(pageInfo.getFontSize() != size){
            pageInfo.setFontSize(size);
            textPaint.setTextSize(size);
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
    public void setOnMenuClickListener(OnMenuClickListener listener){
        mListener = listener;
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
    public void setPageTheme(PageTheme theme){
       fontColor = theme.getFontColor();
       backgroundColor = theme.getBackgroundColor();
       textPaint.setColor(fontColor);
       bottomBarPaint.setColor(fontColor);
       invalidate();
    }
    public void setBackgroundColor(@ColorInt int backgroundColor){
        this.backgroundColor = backgroundColor;
        invalidate();
    }
    public void setFontColor(@ColorInt int fontColor){
        this.fontColor = fontColor;
        textPaint.setColor(fontColor);
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //super.onTouchEvent(event);
        handleTouchEvent(event);
        return true;
    }

    private void handleTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                pressedX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                moved = true;
                break;
            case MotionEvent.ACTION_UP:
                if(moved && Math.abs(event.getX()-pressedX) > (touchSlop*3)){
                    if(event.getX() < pressedX){
                        pageDown();
                    }else{
                        pageUp();
                    }
                    moved = false;
                }else{
                    if(pressedX < mScreenWidth/3){
                        pageUp();
                    }else if(pressedX > mScreenWidth/3*2){
                        pageDown();
                    }else{
                        if(mListener != null){
                            mListener.onOptionMenuClick();
                        }
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                moved = false;
        }
    }

    int times;
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        //canvas.save();
        canvas.drawColor(backgroundColor);
        print(canvas);
        //canvas.restore();
        Log.d("onDraw","invoked:"+times++);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mScreenHeight = getMeasuredHeight();
        mScreenWidth = getMeasuredWidth();
    }

    private void print(Canvas canvas){

        int rowCount = measureTextRows();
        //draw content
        int startPos = pageInfo.getStartPosition();
        for(int i=1;i<=rowCount;i++){
            String nextRow = moveToNextRow(startPos,pageInfo.getRowStartOffset()+pageInfo.getRowEndOffset());
            startPos += nextRow.length();
            canvas.drawText(nextRow,
                    pageInfo.getRowStartOffset(),pageInfo.getFontSize()*i+pageInfo.getContentTopOffset(),textPaint);
        }
        //draw bottom bar
        drawBottomBar(canvas);
    }

    private void pageDown(){
        pageInfo.setStartPosition(pageInfo.getNextStartPosition());
        invalidate();
    }
    private void pageUp(){
        int rowCount = measureTextRows();
        int startPos = pageInfo.getStartPosition();
        for (int i=0;i<rowCount;i++){
            startPos = getLastRowStartPos(startPos, pageInfo.getRowStartOffset()+pageInfo.getRowEndOffset());
        }
        pageInfo.setStartPosition(startPos);
        invalidate();
    }

    private void initializeParams(){
        fontColor = pageInfo.getFontColor();
        backgroundColor = pageInfo.getBackgroundColor();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(pageInfo.getFontSize());
        textPaint.setColor(fontColor);
        bottomBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottomBarPaint.setColor(fontColor);
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

    /**
     * 获取上一行的起点位置
     * @param startPos 开始计算时的起点位置
     * @param offset 行偏移量，用于测算每一行的字数
     * @return 上一行的起点位置
     */
    private int getLastRowStartPos(int startPos,int offset){
        int i = -1;//因为startPos的位置是当前行的行首，故这里i初始值为-1是为将其移动到上一行末尾
        StringBuilder builder = new StringBuilder();
        for(;;){
            int lastPos = startPos + i;
            if(lastPos <= 0){
                return 0;
            }
            char lastChar = PageInfo.bookStr.charAt(lastPos);
            if(lastChar == '\n' && i != -1){//当遇到换行符时则说明已至上行末尾，这里将位置后移1，便是本行之起点
                return lastPos+1;
            }
            builder.append(lastChar);
            if(textPaint.measureText(builder.toString()) < (mScreenWidth - offset)){
                i--;
            }else{//同样的，当某行的字数已经超过一行所能容放的上限时，即表示已到上行，故这里将位置后移1，得到本行起点
                return lastPos+1;
            }
        }
    }

    /**
     * 获取页面行数
     * @return 行数
     */
    private int measureTextRows(){
        int offset = pageInfo.getContentTopOffset()+pageInfo.getContentBottomOffset()+pageInfo.getBottomBarSize();
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

    public interface OnMenuClickListener {

        void onOptionMenuClick();
    }

}
