package com.anarchy.qqpresentation.presentation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateInterpolator;
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
    private Animator mWantAnimator;
    private Animator mDesireAnimator;

    private boolean isWant;
    private boolean isDesire;
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
        setPadding(mHaloTotalWidth, mHaloTotalWidth, mHaloTotalWidth, mHaloTotalWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getChildCount() == 0) return;
        initRadius();
    }

    private void initRadius() {
        Circle circle = (Circle) getChildAt(0);
        mCircleChildRadius = (int) circle.getRadius();
        mBorderRadius = mCircleChildRadius + mBorderWidth / 2;
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
            ObjectAnimator step1 = ObjectAnimator.ofInt(this, mFirstHaloProperty, mCircleChildRadius + mBorderWidth / 2, (mHaloTotalWidth - mBorderWidth) / 3 + mCircleChildRadius + mBorderWidth / 2);
            ObjectAnimator step2 = ObjectAnimator.ofInt(this, mSecondHaloProperty, mCircleChildRadius + mBorderWidth / 2, (mHaloTotalWidth - mBorderWidth) * 2 / 3 + mCircleChildRadius + mBorderWidth / 2);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(step1, step2);
            mShowAnimator = set;
        }
        return mShowAnimator;
    }

    @Override
    public Animator hideHalo() {
        if (mHideAnimator == null) {
            ObjectAnimator step1 = ObjectAnimator.ofInt(this, mFirstHaloProperty, mCircleChildRadius + mBorderWidth / 2);
            ObjectAnimator step2 = ObjectAnimator.ofInt(this, mSecondHaloProperty, mCircleChildRadius + mBorderWidth / 2);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(step1, step2);
            mHideAnimator = set;
        }
        return mHideAnimator;
    }

    @Override
    public void want(boolean want) {
        isWant = want;
        if (mWantAnimator == null) {
            int increment = mHaloTotalWidth / 3;
            ObjectAnimator step1 = ObjectAnimator.ofInt(this, mBorderProperty, mCircleChildRadius, mCircleChildRadius + mBorderWidth / 2 + increment/2, mCircleChildRadius);
            ObjectAnimator step2 = ObjectAnimator.ofInt(this, mFirstHaloProperty, mCircleChildRadius, mCircleChildRadius + (mHaloTotalWidth - mBorderWidth) / 3 + increment, mCircleChildRadius);
            ObjectAnimator step3 = ObjectAnimator.ofInt(this, mSecondHaloProperty, mCircleChildRadius, mCircleChildRadius + (mHaloTotalWidth - mBorderWidth) * 2 / 3 + increment, mCircleChildRadius);
            step1.setRepeatCount(ValueAnimator.INFINITE);
            step2.setRepeatCount(ValueAnimator.INFINITE);
            step3.setRepeatCount(ValueAnimator.INFINITE);
            step1.setDuration(1500);
            step2.setDuration(1500);
            step2.setStartDelay(400);
            step3.setDuration(1500);
            step3.setStartDelay(600);
            step1.setInterpolator(new AccelerateDecelerateInterpolator());
            step2.setInterpolator(new AccelerateDecelerateInterpolator());
            step3.setInterpolator(new AccelerateDecelerateInterpolator());
            AnimatorSet set = new AnimatorSet();
            set.playTogether(step1, step2, step3);
            mWantAnimator = set;
        }
        if (want) {
            mWantAnimator.start();
        } else {
            mWantAnimator.cancel();
            mBorderRadius = mCircleChildRadius + mBorderWidth / 2;
            showHalo().setDuration(400);
            showHalo().start();
        }
    }

    @Override
    public void desire(boolean desire) {
        isDesire = desire;
        if(mDesireAnimator == null){
            int increment = mHaloTotalWidth/4;
            ObjectAnimator step1 = ObjectAnimator.ofInt(this,mBorderProperty,mCircleChildRadius+mBorderWidth/2,mCircleChildRadius+mBorderWidth/2 + increment);
            ObjectAnimator step2 = ObjectAnimator.ofInt(this,mFirstHaloProperty,mCircleChildRadius+mBorderWidth/2 + increment,mCircleChildRadius+mBorderWidth/2 + increment+increment);
            ObjectAnimator step3 = ObjectAnimator.ofInt(this,mSecondHaloProperty,mCircleChildRadius+mBorderWidth/2 + increment+increment,mCircleChildRadius+mBorderWidth/2 + increment+increment+increment);
            ObjectAnimator step4 = ObjectAnimator.ofFloat(this,mPaintProperty,1f,0f);
            step1.setDuration(800);
            step2.setDuration(800);
            step3.setDuration(800);
            step4.setDuration(800);
            step1.setRepeatCount(ValueAnimator.INFINITE);
            step2.setRepeatCount(ValueAnimator.INFINITE);
            step3.setRepeatCount(ValueAnimator.INFINITE);
            step4.setRepeatCount(ValueAnimator.INFINITE);
            step1.setInterpolator(new DecelerateInterpolator(2));
            step2.setInterpolator(new DecelerateInterpolator(2));
            step3.setInterpolator(new DecelerateInterpolator(2));
            AnimatorSet set = new AnimatorSet();
            set.playTogether(step1,step2,step3,step4);
            mDesireAnimator = set;
        }
        if(desire) {
            mDesireAnimator.start();
        }else {
            mDesireAnimator.cancel();
            mBorderRadius = mCircleChildRadius + mBorderWidth / 2;
            mBorderPaint.setColor(mBorderColor);
            mFirstHaloPaint.setColor(mFirstHaloColor);
            mSecondHaloPaint.setColor(mSecondHaloColor);
        }
    }

    @Override
    public int getDesireLeft() {
        return (int) (getX()-mHaloTotalWidth);
    }

    @Override
    public int getDesireRight() {
        return (int) (getX()+getWidth()-mHaloTotalWidth);
    }

    @Override
    public int getDesireTop() {
        return (int) (getY()+mHaloTotalWidth);
    }

    @Override
    public int getDesireBottom() {
        return (int) (getY()+getHeight()-mHaloTotalWidth);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mCircleChildRadius == 0) {
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

    Property<PortraitWrapper, Integer> mFirstHaloProperty = new Property<PortraitWrapper, Integer>(Integer.class, "firstHalo") {
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

    Property<PortraitWrapper, Integer> mSecondHaloProperty = new Property<PortraitWrapper, Integer>(Integer.class, "secondHalo") {
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

    Property<PortraitWrapper, Integer> mBorderProperty = new Property<PortraitWrapper, Integer>(Integer.class, "borderRadius") {
        @Override
        public Integer get(PortraitWrapper object) {
            return object.mBorderRadius;
        }

        @Override
        public void set(PortraitWrapper object, Integer value) {
            if(isWant|| isDesire) {
                object.mBorderRadius = value;
                ViewCompat.postInvalidateOnAnimation(object);
            }
        }
    };
    Property<PortraitWrapper,Float> mPaintProperty = new Property<PortraitWrapper,Float>(Float.class,"paintAlpha"){

        @Override
        public Float get(PortraitWrapper object) {
            return 1f;
        }

        @Override
        public void set(PortraitWrapper object, Float value) {
            object.mBorderPaint.setColor(covertAlpha(value,mBorderColor));
            object.mFirstHaloPaint.setColor(covertAlpha(value,mFirstHaloColor));
            object.mSecondHaloPaint.setColor(covertAlpha(value,mSecondHaloColor));
            ViewCompat.postInvalidateOnAnimation(object);
        }
    };


    private int covertAlpha(float ratio,int color){
        int alpha = (int) (Color.alpha(color)*ratio);
        return  alpha<<24|(color&0xFFFFFF);
    }
}
