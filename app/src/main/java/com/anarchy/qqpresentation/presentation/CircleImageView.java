package com.anarchy.qqpresentation.presentation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.widget.ImageView;

import com.anarchy.qqpresentation.R;
import com.anarchy.qqpresentation.presentation.utils.Util;

public class CircleImageView extends ImageView implements Portrait{

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();


    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;

    private float mDrawableRadius;
    private float mBorderRadius;

    private ColorFilter mColorFilter;

    private boolean mReady;
    private boolean mSetupPending;
    private boolean mDisableCircularTransformation;
    private boolean shouldShow = false;

    private int mBorderCount;
    private int mBorderWidth;
    private int mSavedBorderWidth;
    private int mBorderColor;
    private float mBorderWeakRatio;

    public CircleImageView(Context context) {
        super(context);

        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView,defStyle,0);
        mBorderCount = a.getInt(R.styleable.CircleImageView_BorderCount,0);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_BorderWidth,0);
        mBorderColor = a.getColor(R.styleable.CircleImageView_BorderColor,0xAAFFFFFF);
        mBorderWeakRatio = a.getFraction(R.styleable.CircleImageView_BorderWeakRatio,1,1,0.8f);
        mSavedBorderWidth = mBorderWidth;
        init();
    }

    private void init() {
        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    /**
     * 显示光环
     */
    public Animator showHalo(){
        if(mShowAnimator == null){
            mShowAnimator = ObjectAnimator.ofInt(this, mHaloWidthProperty,0,mSavedBorderWidth);
            mShowAnimator.addListener(mShowListener);
            mShowAnimator.setDuration(400);
        }
       return mShowAnimator;
    }

    /**
     *隐藏光环
     */
    public Animator hideHalo(){
        if(mHideAnimator == null){
            mHideAnimator = ObjectAnimator.ofInt(this,mHaloWidthProperty,mSavedBorderWidth,0);
            mHideAnimator.addListener(mHideListener);
            mHideAnimator.setDuration(400);
        }
       return mHideAnimator;
    }

    private AnimatorListenerAdapter mShowListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            shouldShow = true;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mBorderWidth = mSavedBorderWidth;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
           mBorderWidth = mSavedBorderWidth;
        }
    };
    private AnimatorListenerAdapter mHideListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            shouldShow = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            shouldShow = false;
        }
    };
    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas);
            return;
        }

        if (mBitmap == null) {
            return;
        }


        canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint);
        if (mBorderWidth > 0 && mBorderCount>0&&shouldShow) {
            for(int i=0;i<mBorderCount;i++){
                int alpha = Color.alpha(mBorderColor);
                int red = (mBorderColor>>16)&0xFF;
                int green = (mBorderColor>>8)&0xFF;
                int blue = mBorderColor&0xFF;
                int newAlpha = (int) (Math.pow(mBorderWeakRatio,i)*alpha);
                int newColor = Color.argb(newAlpha,red,green,blue);
                mBorderPaint.setColor(newColor);
                int radius = (int) (mBorderRadius+i*mBorderWidth);
                canvas.drawCircle(mBorderRect.centerX(), mBorderRect.centerY(), radius, mBorderPaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }



    public boolean isDisableCircularTransformation() {
        return mDisableCircularTransformation;
    }

    public void setDisableCircularTransformation(boolean disableCircularTransformation) {
        if (mDisableCircularTransformation == disableCircularTransformation) {
            return;
        }

        mDisableCircularTransformation = disableCircularTransformation;
        initializeBitmap();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initializeBitmap();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initializeBitmap();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        initializeBitmap();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initializeBitmap();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        applyColorFilter();
        invalidate();
    }


    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    private void applyColorFilter() {
        if (mBitmapPaint != null) {
            mBitmapPaint.setColorFilter(mColorFilter);
        }
    }



    private void initializeBitmap() {
        if (mDisableCircularTransformation) {
            mBitmap = null;
        } else {
            mBitmap = Util.getBitmapFromDrawable(getDrawable());
        }
        setup();
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (getWidth() == 0 && getHeight() == 0) {
            return;
        }

        if (mBitmap == null) {
            invalidate();
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        mBorderRect.set(calculateBounds());
        mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth*mBorderCount) / 2.0f, (mBorderRect.width() - mBorderWidth*mBorderCount) / 2.0f);

        mDrawableRect.set(mBorderRect);
        if (mBorderWidth > 0&&mBorderCount>0) {
            mDrawableRect.inset(mBorderWidth*mBorderCount - 1.0f, mBorderWidth*mBorderCount - 1.0f);
        }
        mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f);

        applyColorFilter();
        updateShaderMatrix();
        invalidate();
    }

    private RectF calculateBounds() {
        int availableWidth  = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(availableWidth, availableHeight);

        float left = getPaddingLeft() + (availableWidth - sideLength) / 2f;
        float top = getPaddingTop() + (availableHeight - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    Property<CircleImageView,Integer> mHaloWidthProperty = new Property<CircleImageView, Integer>(Integer.class,"haloWidth") {
        @Override
        public Integer get(CircleImageView object) {
            return object.mBorderWidth;
        }

        @Override
        public void set(CircleImageView object, Integer value) {
            object.mBorderWidth = value;
            ViewCompat.postInvalidateOnAnimation(object);
        }
    };
}
