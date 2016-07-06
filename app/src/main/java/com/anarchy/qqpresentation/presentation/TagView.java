package com.anarchy.qqpresentation.presentation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.anarchy.qqpresentation.presentation.utils.Util;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/1 14:27
 * <p/>
 * 用于显示标签(TextView 并绘制圆形背景)
 * 有3种大小 根据内容的长度计算 长度最长为5个字符
 * 1:一个字符
 * 2:三个字符
 * 3:4~5个字符
 */
class TagView extends View {
    private int originPaddingLeft;
    private int originPaddingRight;
    private int originPaddingTop;
    private int originPaddingBottom;

    private int mBgColor = 0x88888888;
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBgPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private int mTextSize = 30;
    private int mTextColor = Color.WHITE;
    private PresentationLayout.Tag tag;
    private StaticLayout mStaticLayout;
    private Rect mBounds = new Rect();
    private float mRadius;
    boolean shouldWander = true;

    public TagView(Context context) {
        super(context);
        init();
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        mTextPaint.setTextSize(mTextSize);
//        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(0x88888888);
    }

    public void initOriginPadding(int left,int top,int right, int bottom){
        originPaddingBottom = bottom;
        originPaddingLeft = left;
        originPaddingRight = right;
        originPaddingTop = top;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    void setSource(PresentationLayout.Tag tag){
        this.tag = tag;
        String summary = tag.getTag();
        int count = tag.getCount();
        String countWithBracket;
        if(count<=99){
            countWithBracket = "("+count+")";
        }else {
            countWithBracket = "(99+)";
        }
        String source;
        int width;
        if(summary.length()<=3){
            source = summary+"\n"+countWithBracket;
            width = (int) Math.max(mTextPaint.measureText(countWithBracket),mTextPaint.measureText(summary));

        }else {
            source = summary.substring(0,2)+"\n"+summary.substring(2)+"\n"+countWithBracket;
            width = (int) Math.max(mTextPaint.measureText(countWithBracket),mTextPaint.measureText(summary.substring(0,3)));
        }
        mStaticLayout = new StaticLayout(source,mTextPaint,width, Layout.Alignment.ALIGN_CENTER,1f,0,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mStaticLayout == null){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        }else {//不关心parent的属性 来测量高宽
            int width = mStaticLayout.getWidth() + originPaddingLeft + originPaddingRight;
            int height = mStaticLayout.getHeight() + originPaddingTop + originPaddingBottom;
            int diameter = (int) Math.hypot(width, height);
            mRadius = 0.5f*diameter;
            int paddingLeft = (diameter - width) / 2 + originPaddingLeft;
            int paddingRight = (diameter - width) / 2 + originPaddingRight;
            int paddingTop = (diameter - height) / 2 + originPaddingTop;
            int paddingBottom = (diameter - height) / 2 + originPaddingBottom;
            setPivotX(mRadius);
            setPivotY(mRadius);
            setPadding(paddingLeft,paddingTop,paddingRight,paddingBottom);
            setMeasuredDimension(diameter, diameter);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawCircle(mRadius,mRadius,mRadius,mBgPaint);
        canvas.translate(getPaddingLeft(),getPaddingTop());
        mStaticLayout.draw(canvas);
        canvas.restore();
    }


    /**
     * 设置背景颜色
     * @param color
     */
    public void setBgColor(int color){
        mBgColor = color;
        Drawable bg = getBackground();
        if(bg instanceof ShapeDrawable){
            ((ShapeDrawable) bg).getPaint().setColor(mBgColor);
        }else {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.getPaint().setColor(mBgColor);
            Util.setBackground( this,shapeDrawable);
        }
    }

    /**
     * 获取背景颜色
     * @return
     */
    public int getBgColor(){
        return  mBgColor;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mTextPaint.setTextSize(textSize);
        invalidate();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }

    void setPoint(PointF pointF){
        float left = pointF.x - getWidth()/2;
        float top = pointF.y - getHeight()/2;
        setX(left);
        setY(top);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
    }

    private PointF mPointF = new PointF();
    PointF getPoint(){
        mPointF.set(getLeft()+getWidth()/2,getTop()+getHeight()/2);
        return mPointF;
    }
}
