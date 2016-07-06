package com.anarchy.qqpresentation.presentation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.anarchy.qqpresentation.R;


/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/6 17:02
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 */
public class PortraitWrapper extends FrameLayout implements Portrait {
    private int mBorderColor;
    private int mFirstHaloColor;
    private int mSecondHaloColor;
    private int mBorderRadius;
    private int mFirstHaloRadius;
    private int mSecondHaloRadius;
    private int mBorderWidth;
    private int mHaloTotalWidth;
    private int mCircleChildRadius;
    private Paint mBorderPaint;
    private Paint mFirstHaloPaint;
    private Paint mSecondHaloPaint;

    private Animator mShowAnimator;
    private Animator mHideAnimator;

    public PortraitWrapper(Context context) {
        this(context, null);
    }

    public PortraitWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PortraitWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PortraitWrapper, defStyleAttr, R.style.Default_Portrait);
        mBorderColor = a.getColor(R.styleable.PortraitWrapper_BorderColor, Color.WHITE);
        mFirstHaloColor = a.getColor(R.styleable.PortraitWrapper_FirstHaloColor, Color.WHITE);
        mSecondHaloColor = a.getColor(R.styleable.PortraitWrapper_SecondHaloColor, Color.WHITE);
        mHaloTotalWidth = a.getDimensionPixelOffset(R.styleable.PortraitWrapper_HaloTotalWidth, 100);
        mBorderWidth = a.getDimensionPixelOffset(R.styleable.PortraitWrapper_BorderWidth, 5);
        a.recycle();
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(mBorderColor);
        mFirstHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstHaloPaint.setColor(mFirstHaloColor);
        mFirstHaloPaint.setStyle(Paint.Style.FILL);
        mSecondHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHaloPaint.setColor(mSecondHaloColor);
        mSecondHaloPaint.setStyle(Paint.Style.FILL);
        setPadding(mHaloTotalWidth,mHaloTotalWidth,mHaloTotalWidth,mHaloTotalWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getChildCount() == 0) return;
        initRadius();
    }
    private void initRadius(){
        Circle circle = (Circle) getChildAt(0);
        mCircleChildRadius = (int) circle.getRadius();
        mBorderRadius = mCircleChildRadius + mBorderWidth/2;
        mFirstHaloRadius = mCircleChildRadius;
        mSecondHaloRadius = mCircleChildRadius;
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("PortraitWrapper can host only one direct child");
        }
        if (!(child instanceof Circle)) {
            throw new IllegalStateException("child require implement Circle");
        }
        ((LayoutParams) params).gravity = Gravity.CENTER;
        super.addView(child, index, params);
    }

    @Override
    public Animator showHalo() {
        if (mShowAnimator == null) {
            ObjectAnimator step1 = ObjectAnimator.ofInt(this,mFirstHaloProperty,(mHaloTotalWidth-mBorderWidth)/3+mBorderRadius);
            ObjectAnimator step2 = ObjectAnimator.ofInt(this,mSecondHaloProperty,(mHaloTotalWidth-mBorderWidth)*2/3+mBorderRadius);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(step1,step2);
            mShowAnimator = set;
        }
        return mShowAnimator;
    }

    @Override
    public Animator hideHalo() {
        if (mHideAnimator == null) {
            ObjectAnimator step1 = ObjectAnimator.ofInt(this,mFirstHaloProperty,mBorderRadius);
            ObjectAnimator step2 = ObjectAnimator.ofInt(this,mSecondHaloProperty,mBorderRadius);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(step1,step2);
            mHideAnimator = set;
        }
        return mHideAnimator;
    }

    @Override
    public Animator want() {

        return null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(mCircleChildRadius == 0){
            initRadius();
        }
        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;
        //依次绘制三个圆
        canvas.drawCircle(cx, cy, mSecondHaloRadius, mSecondHaloPaint);
        canvas.drawCircle(cx, cy, mFirstHaloRadius, mFirstHaloPaint);
        canvas.drawCircle(cx, cy, mBorderRadius, mBorderPaint);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
    }

    Property<PortraitWrapper, Integer> mFirstHaloProperty = new Property<PortraitWrapper, Integer>(Integer.class,"firstHalo") {
        @Override
        public Integer get(PortraitWrapper object) {
            return object.mFirstHaloRadius;
        }

        @Override
        public void set(PortraitWrapper object, Integer value) {
            object.mFirstHaloRadius = value;
            ViewCompat.postInvalidateOnAnimation(object);
        }
    };

    Property<PortraitWrapper,Integer> mSecondHaloProperty = new Property<PortraitWrapper, Integer>(Integer.class,"secondHalo") {
        @Override
        public Integer get(PortraitWrapper object) {
            return object.mSecondHaloRadius;
        }

        @Override
        public void set(PortraitWrapper object, Integer value) {
            object.mSecondHaloRadius = value;
            ViewCompat.postInvalidateOnAnimation(object);
        }
    };

    Property<PortraitWrapper,Integer> mBorderProperty = new Property<PortraitWrapper, Integer>(Integer.class,"borderRadius") {
        @Override
        public Integer get(PortraitWrapper object) {
            return object.mBorderRadius;
        }

        @Override
        public void set(PortraitWrapper object, Integer value) {
            object.mBorderRadius = value;
            ViewCompat.postInvalidateOnAnimation(object);
        }
    };
}
