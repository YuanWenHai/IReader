package com.will.Stardust.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Will on 2016/2/2.
 */
public class PageView extends View {
    private Bitmap bit;
    public PageView(Context context){
        super(context);
    }
    public PageView(Context context, AttributeSet attr){
        super(context,attr);
    }
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.save();
        //canvas.drawColor(Color.BLUE);
        canvas.drawBitmap(bit,0,0,null);
        canvas.restore();
    }
    public void setBitmap(Bitmap bitmap){
            bit = bitmap;
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }
}
