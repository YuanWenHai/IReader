package com.will.ireader.printer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.will.ireader.common.Util;

/**
 * created  by will on 2019/5/11 16:54
 */
public class Page extends View {

    private String[] lines;
    private int lineCount = 0;
    private int rowCount = 0;
    private int availableWidth;

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
        com.will.ireader.printer.Book b = new com.will.ireader.printer.Book("test.txt","/storage/emulated/0/test.txt");
        printer = new Printer(b);


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(px(mConfig.getFontSize()));
    }

    private int px(int dp){
        return Util.getPXFromDP(dp);
    }

    private void initializePageSpec(){
        rowCount = (getMeasuredHeight() - px(mConfig.getContentPaddingVertical() * 2 + mConfig.getInfoPaddingVertical() * 2 + mConfig.getInfoFontSize()))/ (px(mConfig.getFontSize() + mConfig.getLineSpacing()));
        availableWidth = getMeasuredWidth() - px(mConfig.getContentPaddingHorizontal() * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for(int i=1;i<=rowCount;i++){
            canvas.drawText(printer.printLineForward(availableWidth,paint),px(mConfig.getContentPaddingHorizontal()),px(mConfig.getFontSize()) * i,paint);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initializePageSpec();
    }












    public class PageConfig {
        private int fontSize = 16;
        //private int fontSpacing = 2;
        private int contentPaddingHorizontal = 2;
        private int contentPaddingVertical = 2;
        private int lineSpacing = 1;

        private int infoFontSize = 4;
        private int infoPaddingHorizontal = 2;
        private int infoPaddingVertical = 2;

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
