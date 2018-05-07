package com.will.ireader.view.pageview;

import android.graphics.Color;

import com.will.ireader.R;

/**
 * Created by will on 2018/1/28.
 */

public enum PageTheme {
    DAY_THEME{
        @Override
        public int getFontColor() {
            return Color.rgb(0x42,0x42,0x42);
        }

        @Override
        public int getBackgroundColor() {
            return Color.rgb(0xfc,0xec,0xef);
        }

        @Override
        public int getWidgetColor() {
            return Color.rgb(0x90,0xca,0xf9);
        }

        @Override
        public int getSheetColor() {
            return Color.rgb(0x21,0x96,0xf3);
        }

        @Override
        public int getThemeIconRes() {
            return R.drawable.ic_brightness_7_white_24dp;
        }
        @Override
        public int getToolbarColor() {
            return Color.rgb(0x1E,0x88,0xE5);
        }

    },


    NIGHT_THEME{
        @Override
        public int getFontColor() {
            return Color.rgb(0xe0,0xe0,0xe0);
        }

        @Override
        public int getBackgroundColor() {
            return Color.rgb(0x42,0x42,0x42);
        }

        @Override
        public int getWidgetColor() {
            return Color.rgb(0x75,0x75,0x75);
        }

        @Override
        public int getSheetColor() {
            return Color.rgb(0x55,0x55,0x55);
        }

        @Override
        public int getThemeIconRes() {
            return R.drawable.ic_brightness_6_white_24dp;
        }

        @Override
        public int getToolbarColor() {
            return Color.rgb(0x42,0x42,0x42);
        }

    };
    public abstract int getFontColor();
    public abstract int getBackgroundColor();
    public abstract int getWidgetColor();
    public abstract int getSheetColor();
    public abstract int getThemeIconRes();
    public abstract int getToolbarColor();
    public boolean equals(PageTheme instance){
        return  instance != null && instance == this;
    }
}
