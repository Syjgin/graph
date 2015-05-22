package com.example.syjgin.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;


/**
 * this view draws single graphic column with RectShape
 */
public class RectView extends View {
    private ShapeDrawable mDrawable;
    private int mValue;

    public RectView(Context context) {
        super(context);
    }

    public void init(int w, int h, int color, int value) {
        // draw background from top, because it's seems to no way to draw rect from bottom
        mValue = value;

        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(color);
        mDrawable.setBounds(0, 0, w, h);
        setWillNotDraw(false);
    }

    public int getValue() {
        return mValue;
    }



    protected void onDraw(Canvas canvas) {
        if(mDrawable != null)
            mDrawable.draw(canvas);
    }
}
