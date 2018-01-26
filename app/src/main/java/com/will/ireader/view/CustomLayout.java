package com.will.ireader.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.will.ireader.R;
import com.will.ireader.common.Util;

/**
 * Created by will on 2017/12/20.
 */

public class CustomLayout extends RelativeLayout {
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mRect = new Rect(0,0,0,0);
    private int mHeight;
    private int mWidth;
    private int mPercentage;
    private TextView deleteButton;
    private OnClickListener deleteButtonClickListener;


    public CustomLayout(Context context) {
        super(context);
    }

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,R.styleable.CustomLayout,0,0);
        int bgColor = context.getResources().getColor(typedArray.getResourceId(R.styleable.CustomLayout_backgroundColor,R.color.colorPrimary));
        mPaint.setColor(bgColor);
        typedArray.recycle();
        setWillNotDraw(false);
    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void drawCustomBackground(Canvas canvas){
        mRect.set(0,0,mWidth*mPercentage/100,mHeight);
        canvas.drawRect(mRect,mPaint);
    }

    public void setCustomBackgroundColor(@ColorInt int color){
        mPaint.setColor(color);
        invalidate();
    }
    /**
     * range is 0 - 100
     * @param percentage p
     */
    public void setPercentage(int percentage){
        mPercentage = Math.max(0,Math.min(100,percentage));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCustomBackground(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addDeleteButtonToLayout();
    }


    private void addDeleteButtonToLayout(){
        deleteButton = new TextView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        deleteButton.setLayoutParams(params);
        deleteButton.setPadding(Util.getPXFromDP(getContext(),20),0,Util.getPXFromDP(getContext(),20),0);
        deleteButton.setGravity(Gravity.CENTER);
        deleteButton.setText("删除");
        deleteButton.setTextColor(Color.WHITE);
        deleteButton.setTextSize(16);
        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setBackgroundColor(Util.getColorFromRes(getContext(),R.color.materialRed));
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteButtonClickListener != null){
                    deleteButtonClickListener.onClick(v);
                }
            }
        });
        addView(deleteButton);
    }
    public void showDeleteButton(){
        if(isDeleteButtonVisible()){
            return;
        }
        TranslateAnimation animation = new TranslateAnimation(deleteButton.getMeasuredWidth(),0,0,0);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                deleteButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        deleteButton.startAnimation(animation);
    }
    public void hideDeleteButton(){
        if(!isDeleteButtonVisible()){
            return;
        }
        TranslateAnimation animation = new TranslateAnimation(0,deleteButton.getMeasuredWidth(),0,0);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                deleteButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        deleteButton.startAnimation(animation);
    }
    public boolean isDeleteButtonVisible(){
        return deleteButton.getVisibility() == View.VISIBLE;
    }

    public void setOnDeleteButtonClickListener(OnClickListener listener){
        deleteButtonClickListener = listener;
    }

}
