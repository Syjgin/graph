package com.example.syjgin.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;


/**
 * this view displays column selector on click
 */
public class SelectorView extends View {
    private ShapeDrawable mDrawable;
    private int mXCoord;

    public SelectorView(Context context) {
        super(context);
    }

    public void init(int x, int height) {

        mXCoord = x;
        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setStyle(Paint.Style.FILL);
        mDrawable.getPaint().setColor(Color.GRAY);
        mDrawable.getPaint().setAlpha(128);
        mDrawable.setBounds(x, 0, x + MainActivity.COLUMN_WIDTH, height);
        setWillNotDraw(false);
    }

    public int getXCoord() {
        return mXCoord;
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }
}
