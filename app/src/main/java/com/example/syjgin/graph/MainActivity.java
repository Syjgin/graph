package com.example.syjgin.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private static final int VALUES_COUNT = 20000;
    private static final int MAX_VALUE = 100000;
    private static final int ANIM_DURATION = 500;
    private static final float BORDER_COEF = 0.7f;

    private int mChartHeight;
    private int mScreenWidth;
    private boolean mIsNormalizationStarted = false;

    public static final int COLUMN_WIDTH = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        mChartHeight = (int)(displaySize.y *BORDER_COEF);
        mScreenWidth = displaySize.x;

        ScrollChangedListener scrollListener = new ScrollChangedListener();
        ScrollStopListener stopListener = new ScrollStopListener();
        IndicatorScrollView scroll = (IndicatorScrollView)findViewById(R.id.horizontalScrollView);
        scroll.setOnScrollChangedListener(scrollListener);
        scroll.setOnScrollStopListener(stopListener);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        new PopulateListTask().execute();
    }

    public int getChartHeight() {
        return mChartHeight;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ChangeColumnsAppearance(boolean resize, IndicatorScrollView scroll, LinearLayout itemsParent) {
        //used by scroll listeners: resize columns on stop or revert scale on move start
        Rect scrollBounds = new Rect();
        scroll.getHitRect(scrollBounds);
        int childCount = itemsParent.getChildCount();
        ArrayList<RectView> visibleRects = new ArrayList<RectView>();
        for(int i = 0;i < childCount; i++) {
            RectView currentItem = (RectView)itemsParent.getChildAt(i);
            if(currentItem.getLocalVisibleRect(scrollBounds)) {
                visibleRects.add(currentItem);
            }
        }
        int currentMaxValue = 0;
        RectView maxValueRect = null;
        for (int i =0; i < visibleRects.size(); i++) {
            int currentValue = visibleRects.get(i).getValue();
            if(currentValue > currentMaxValue) {
                currentMaxValue = currentValue;
                maxValueRect = visibleRects.get(i);
            }
        }
        float maxValueCoef = resize ? 0 : 1;
        float otherValuesCoef = resize ? (float)currentMaxValue/MAX_VALUE : 1;
        if(maxValueRect != null) {
            ScaleAnimation maxValueAnimation = new ScaleAnimation(1,1,maxValueRect.getScaleY(),maxValueCoef);
            maxValueAnimation.setInterpolator(new LinearInterpolator());
            maxValueAnimation.setDuration(ANIM_DURATION);
            maxValueAnimation.setFillAfter(true);
            maxValueAnimation.setFillEnabled(true);
            maxValueAnimation.setFillBefore(false);
            maxValueRect.setAnimation(maxValueAnimation);
            maxValueRect.startAnimation(maxValueAnimation);
            for (int i =0; i < visibleRects.size(); i++) {
                if(visibleRects.get(i) != maxValueRect) {
                    ScaleAnimation valueAnimation = new ScaleAnimation(1,1,visibleRects.get(i).getScaleY(),otherValuesCoef);
                    valueAnimation.setInterpolator(new LinearInterpolator());
                    valueAnimation.setDuration(ANIM_DURATION);
                    valueAnimation.setFillAfter(true);
                    valueAnimation.setFillEnabled(true);
                    valueAnimation.setFillBefore(false);
                    visibleRects.get(i).setAnimation(valueAnimation);
                    visibleRects.get(i).startAnimation(valueAnimation);
                }
            }
            DimensionView dimensionView = (DimensionView)findViewById(R.id.dimensionView);
            if(resize)
                dimensionView.refreshLabels(currentMaxValue);
            else
                dimensionView.refreshLabels(MAX_VALUE);
        }
    }

    private class ScrollChangedListener implements IndicatorScrollView.OnScrollChangedListener {

        private TextView mIndicator;
        private SelectorView mSelector = null;
        private LinearLayout mChildLayout;
        private IndicatorScrollView mScroll;

        public ScrollChangedListener() {
            mIndicator = (TextView) findViewById(R.id.indicator);
            mChildLayout = (LinearLayout) findViewById(R.id.mainLayout);
            mScroll = (IndicatorScrollView)findViewById(R.id.horizontalScrollView);
        }

        @Override
        public void onScrollChanged(IndicatorScrollView v, int l, int t, int oldl, int oldt) {
            if(!mIsNormalizationStarted) {
                ChangeColumnsAppearance(false, mScroll, mChildLayout);
                mIsNormalizationStarted = true;
            }
            mSelector = (SelectorView) findViewById(R.id.selector_id);
            if(mSelector != null) {
                float targetLeft = mSelector.getXCoord();
                int childCount = mChildLayout.getChildCount();
                for(int i=0; i < childCount; i++) {
                    RectView view = (RectView)mChildLayout.getChildAt(i);
                    int viewLeft = view.getLeft() - v.getScrollX();
                    if(viewLeft >= targetLeft) {
                        mIndicator.setText(String.valueOf(view.getValue()));
                        break;
                    }
                }
            }
        }
    }

    private class ScrollStopListener implements IndicatorScrollView.OnScrollStoppedListener {

        private LinearLayout mItemsParent;
        private IndicatorScrollView mScroll;
        public ScrollStopListener() {
            mItemsParent = (LinearLayout)findViewById(R.id.mainLayout);
            mScroll = (IndicatorScrollView)findViewById(R.id.horizontalScrollView);
        }
        @Override
        public void onScrollStopped() {
            ChangeColumnsAppearance(true, mScroll, mItemsParent);
            mIsNormalizationStarted = false;
        }
    }

    private class ColumnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            MainActivity activity = MainActivity.this;
            int scrollX = ((IndicatorScrollView)v.getParent().getParent()).getScrollX();
            int targetX = v.getLeft() - scrollX;

            SelectorView view = new SelectorView(activity);
            view.init(targetX, activity.getChartHeight());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, activity.getChartHeight());
            lp.gravity = Gravity.BOTTOM;
            view.setLayoutParams(lp);
            SelectorView oldSelector = (SelectorView) findViewById(R.id.selector_id);
            if(oldSelector != null)
                ((LinearLayout)oldSelector.getParent()).removeView(oldSelector);

            view.setId(R.id.selector_id);
            LinearLayout selectorLayout = (LinearLayout)findViewById(R.id.selectorLayout);
            selectorLayout.addView(view);
            TextView indicator = (TextView)findViewById(R.id.indicator);
            indicator.setText(String.valueOf(((RectView)v).getValue()));
        }
    }

    private class PopulateListTask extends AsyncTask<Void, Void, Void> {

        private Random generator;
        private int[] valuesArray;

        @Override
        protected Void doInBackground(Void... params) {

            generator = new Random(System.currentTimeMillis());
            valuesArray = new int[VALUES_COUNT];
            for(int i = 0; i < VALUES_COUNT; i++) {
                valuesArray[i] = generator.nextInt(MAX_VALUE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            try {
                MainActivity activity = MainActivity.this;

                float coef = (float) activity.getChartHeight() / MAX_VALUE;
                LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);

                TypedArray array = getTheme().obtainStyledAttributes(new int[]{
                        android.R.attr.colorBackground,
                        android.R.attr.textColorPrimary,
                });
                int backgroundColor = array.getColor(0, 0xFF00FF);
                array.recycle();
                DimensionView dimensionView = (DimensionView)findViewById(R.id.dimensionView);
                dimensionView.init(activity.getChartHeight(), activity.getScreenWidth(), MAX_VALUE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, activity.getChartHeight());
                params.gravity = Gravity.LEFT|Gravity.BOTTOM;
                dimensionView.setLayoutParams(params);

                layout.setBackgroundColor(Color.CYAN);
                FrameLayout.LayoutParams mainLayoutParams = (FrameLayout.LayoutParams)layout.getLayoutParams();
                mainLayoutParams.height = activity.getChartHeight();
                mainLayoutParams.gravity = Gravity.BOTTOM;
                for (int i =0; i < valuesArray.length; i++) {

                    RectView view = new RectView(activity);
                    view.init(COLUMN_WIDTH, (int) ((MAX_VALUE - valuesArray[i]) * coef), backgroundColor, valuesArray[i]);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(COLUMN_WIDTH, activity.getChartHeight());
                    lp.gravity = Gravity.BOTTOM;
                    view.setLayoutParams(lp);
                    view.setOnClickListener(new ColumnClickListener());
                    layout.addView(view);
                }
                ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
                ((LinearLayout)progressBar.getParent()).removeView(progressBar);
                IndicatorScrollView scroll = (IndicatorScrollView)findViewById(R.id.horizontalScrollView);
                scroll.startCheckScrolling();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
