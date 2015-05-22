package com.example.syjgin.graph;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;


/**
 * ScrollView with scroll change and scroll stop events listeners
 */
public class IndicatorScrollView extends HorizontalScrollView {

    private int mInitialPos;
    private Runnable scrollStopDetectTask;
    private static final int CHECK_PERIOD = 100;
    public IndicatorScrollView(Context context) {
        super(context);
        createStopCheckTask();
    }

    public IndicatorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createStopCheckTask();
    }

    public IndicatorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createStopCheckTask();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IndicatorScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createStopCheckTask();
    }

    private void createStopCheckTask() {
        //after scroll starts, periodically check distance between old and new positions. If it equals zero, invoke scroll stop listener and exit
        scrollStopDetectTask = new Runnable() {
            @Override
            public void run() {
                int newPos = getScrollX();
                if((mInitialPos - newPos) == 0) {
                    if(mOnScrollStoppedListener != null){

                        mOnScrollStoppedListener.onScrollStopped();
                    }
                } else {
                    mInitialPos = getScrollX();
                    IndicatorScrollView.this.postDelayed(this, IndicatorScrollView.CHECK_PERIOD);
                }
            }
        };
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    startCheckScrolling();
                }

                return false;
            }
        });
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(mOnScrollChangedListener != null)
            mOnScrollChangedListener.onScrollChanged( this, l, t, oldl, oldt );
        super.onScrollChanged(l, t, oldl, oldt);
    }

    private OnScrollChangedListener mOnScrollChangedListener;
    private OnScrollStoppedListener mOnScrollStoppedListener;

    public void setOnScrollChangedListener(OnScrollChangedListener l) {
        this.mOnScrollChangedListener = l;
    }
    public void setOnScrollStopListener(OnScrollStoppedListener l) {
        this.mOnScrollStoppedListener = l;
    }

    public void startCheckScrolling() {
        mInitialPos = getScrollX();
        IndicatorScrollView.this.postDelayed(scrollStopDetectTask, IndicatorScrollView.CHECK_PERIOD);
    }

    public interface OnScrollChangedListener {
        void onScrollChanged( IndicatorScrollView v, int l, int t, int oldl, int oldt );
    }

    public interface OnScrollStoppedListener{
        void onScrollStopped();
    }
}
