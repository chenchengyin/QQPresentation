package com.anarchy.qqpresentation.presentation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
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
    private int mTextSize = 15;
    private int mTextColor = Color.WHITE;
    private PresentationLayout.Tag tag;
    private StaticLayout mStaticLayout;
    private Rect mBounds = new Rect();


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
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
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
        mStaticLayout = new StaticLayout(source,mTextPaint,width, Layout.Alignment.ALIGN_CENTER,1,0,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mStaticLayout == null){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        }else {//封闭的测量高宽
            int width = mStaticLayout.getWidth();
            int height = mStaticLayout.getHeight();
            originPaddingBottom = getPaddingBottom();
            originPaddingLeft = getPaddingLeft();
            originPaddingRight = getPaddingRight();
            originPaddingTop = getPaddingTop();
            int diameter = (int) Math.hypot(width, height);
            int paddingLeft = (diameter - width) / 2 + originPaddingLeft;
            int paddingRight = (diameter - width) / 2 + originPaddingRight;
            int paddingTop = (diameter - height) / 2 + originPaddingTop;
            int paddingBottom = (diameter - height) / 2 + originPaddingBottom;
            setPadding(paddingLeft,paddingTop,paddingRight,paddingBottom);
            setMeasuredDimension(diameter, diameter);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
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
}
