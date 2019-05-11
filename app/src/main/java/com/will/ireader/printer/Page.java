package com.will.ireader.printer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.will.ireader.common.Util;

import java.io.File;

/**
 * created  by will on 2019/5/11 16:54
 */
public class Page extends View {

    private String[] lines;
    private int lineCount = 0;
    private int rowCount = 0;
    private PageConfig mConfig;

    public Page(Context context) {
        super(context);
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

    private void initializePageSpec(){
        this.lineCount = (getMeasuredHeight()-Util.getPXFromDP(mConfig.getContentPaddingVertical()))/(Util.getPXFromDP(mConfig.getFontSize())+ mConfig.lineSpacing);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public class PageConfig {
        private int fontSize = 16;
        private int fontSpacing = 2;
        private int contentPaddingHorizontal = 10;
        private int contentPaddingVertical = 10;
        private int lineSpacing = 2;

        private int infoFontSize = 16;
        private int infoPaddingHorizontal = 10;
        private int infoPaddingVertical = 10;

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public int getFontSpacing() {
            return fontSpacing;
        }

        public void setFontSpacing(int fontSpacing) {
            this.fontSpacing = fontSpacing;
        }

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
