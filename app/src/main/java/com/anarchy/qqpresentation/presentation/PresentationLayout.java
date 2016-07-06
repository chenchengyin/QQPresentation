package com.anarchy.qqpresentation.presentation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.anarchy.qqpresentation.R;
import com.anarchy.qqpresentation.presentation.evaluator.PathEvaluator;
import com.anarchy.qqpresentation.presentation.utils.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/1 14:16
 * <p/>
 * 效果有为4个状态:
 * 1.Collapsed: 背景图片 中央圆形头像展示 头像下方信息展示
 * 2.Expanding: 大小扩展为整个屏幕->模糊背景图片头像显示光圈->标签从头像中心扩展四散
 * 3.Expanded: 标签圆形维持在固定位置一定范围内晃动
 * 4.Collapsing: 去模糊 及 圆形缩小并聚集在中心 圆形头像关闭光圈
 */
public class PresentationLayout extends RelativeLayout {
    private static final String TAG = "PresentationLayout";
    /**
     * 对应各个标签的绘制的角度位置
     */
    private final static double[] RADIUS = new double[]{0.7d, 2.25d, 3.14d, 5.5d, 0.1d, 1.4d, 4.7d};
    private static final int STATE_COLLAPSED = 0;
    private static final int STATE_EXPANDING = 1;
    private static final int STATE_EXPANDED = 2;
    private static final int STATE_COLLAPSING = 3;
    private static final double CONTROL_RADIANS_OFFSET = 1.5d;
    private int mState = STATE_COLLAPSED;
    private Portrait mPortrait;
    private ViewDragHelper mDragHelper;//使用dragHelper 实现 tagView的拖动
    private int mCollapsedHeight;//默认高度，比如在xml中设置的高度
    private int mExpandHeight;//展开时需要的高度 未设置则取父view的高度即设置Layout_height = "match_parent"
    private AnimatorSet mExpandAnimator;
    private AnimatorSet mCollapsedAnimator;
    private Drawable mOriginBackground;
    private Drawable mBluredBackground;
    private View mBackgroundOverlay;//用来显示做模糊背景效果
    private int mThickness;//圆环厚度 也是tagView 圆心可活动范围, 初始范围为thickness 中间值
    private int mInnerRadius;//圆环内部半径
    private int mTagPadding;//tagView 的padding值
    private TagViewProperty mTagViewProperty = new TagViewProperty(PointF.class, "point");
    private List<StateChangeListener> mStateChangeListenerList;
    private int capturedLeft;
    private int capturedTop;
    private boolean mSlideEnable = true;
    private int mSlideLength;
    private float[] mTargets = new float[14];
    private boolean isCaptured = false;
    private int mTagViewTextSize;
    private int mTagViewBackgroundColor;
    private int mTagViewBorderColor;
    private int mTagViewBorderWidth;
    private int mTagViewTextColor;
    private boolean inDesire = false;



    private List<TagView> mTagViews;

    public PresentationLayout(Context context) {
        this(context, null);
    }

    public PresentationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public PresentationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mBackgroundOverlay = new View(context);
        mBackgroundOverlay.setVisibility(GONE);
        addViewInLayout(mBackgroundOverlay, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
                    @Override
                    public boolean tryCaptureView(View child, int pointerId) {
                        return false;
                    }

                    @Override
                    public int clampViewPositionHorizontal(View child, int left, int dx) {
                        return left;
                    }

                    @Override
                    public int clampViewPositionVertical(View child, int top, int dy) {
                        return top;
                    }

                    @Override
                    public void onViewCaptured(View capturedChild, int activePointerId) {
                        if(capturedChild instanceof TagView){
                            isCaptured = true;
                            ((TagView) capturedChild).shouldWander = false;
                            mPortrait.want(true);
                            capturedLeft = capturedChild.getLeft();
                            capturedTop = capturedChild.getTop();

                        }
                    }

                    @Override
                    public void onViewReleased(final View releasedChild, float xvel, float yvel) {
                        if(releasedChild instanceof TagView){
                            isCaptured = false;
                            float x = releasedChild.getX() + releasedChild.getWidth() / 2;
                            float y = releasedChild.getY() + releasedChild.getHeight() / 2;
                            if(x>=mPortrait.getDesireLeft()&&x<=mPortrait.getDesireRight()&&y>=mPortrait.getDesireTop()&&y<=mPortrait.getDesireBottom()) {
                                mPortrait.desire(false);
                                ObjectAnimator insert = ObjectAnimator.ofPropertyValuesHolder(releasedChild,PropertyValuesHolder.ofFloat("scaleX",1f,0f,1f),
                                        PropertyValuesHolder.ofFloat("scaleY",1f,0f,1f));
                                insert.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        TagView tagView = (TagView) releasedChild;
                                        tagView.tag.count++;
                                        tagView.setSource(tagView.tag);
                                        tagView.invalidate();
                                        settling(tagView);
                                    }
                                });
                                insert.setDuration(400);
                                insert.start();
                            }else {
                                mPortrait.want(false);
                                settling((TagView) releasedChild);
                            }
                        }
                    }

                    @Override
                    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                        if(isCaptured) {
                            float x = changedView.getX() + changedView.getWidth() / 2;
                            float y = changedView.getY() + changedView.getHeight() / 2;
                            boolean desire;
                            if(x>=mPortrait.getDesireLeft()&&x<=mPortrait.getDesireRight()&&y>=mPortrait.getDesireTop()&&y<=mPortrait.getDesireBottom()){
                                desire = true;
                                if(desire^inDesire){
                                    inDesire = desire;
                                    mPortrait.want(false);
                                    mPortrait.desire(true);
                                }
                            }else {
                                desire = false;
                                if(desire^inDesire){
                                    inDesire = desire;
                                    mPortrait.desire(false);
                                    mPortrait.want(true);
                                }
                            }
                        }
                    }
                }
        );
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PresentationLayout, defStyleAttr, R.style.Default_Presentation);
        mInnerRadius = a.getDimensionPixelOffset(R.styleable.PresentationLayout_InnerRadius, 300);
        mThickness = a.getDimensionPixelOffset(R.styleable.PresentationLayout_Thickness, 30);
        mTagPadding = a.getDimensionPixelOffset(R.styleable.PresentationLayout_TagPadding, 10);
        mSlideEnable = a.getBoolean(R.styleable.PresentationLayout_SlideEnable,true);
        mSlideLength = a.getDimensionPixelOffset(R.styleable.PresentationLayout_SlideLength,20);
        mTagViewTextColor = a.getColor(R.styleable.PresentationLayout_TagViewTextColor, Color.WHITE);
        mTagViewTextSize = a.getDimensionPixelOffset(R.styleable.PresentationLayout_TagViewTextSize,30);
        mTagViewBackgroundColor = a.getColor(R.styleable.PresentationLayout_TagViewBackgroundColor,0x88888888);
        mTagViewBorderColor = a.getColor(R.styleable.PresentationLayout_TagViewBorderColor,0xFFFCFCFC);
        mTagViewBorderWidth = a.getDimensionPixelOffset(R.styleable.PresentationLayout_TagViewBorderWidth,3);
        a.recycle();
        post(mDoBlurRunnable);
        initStateChangeListener();
    }

    private void settling(TagView releasedChild){
         releasedChild.shouldWander = true;
        mPortrait.want(false);
        mDragHelper.smoothSlideViewTo(releasedChild, capturedLeft, capturedTop);
        ViewCompat.postInvalidateOnAnimation(PresentationLayout.this);
    }

    public void setSlideEnable(boolean enable){
        mSlideEnable = enable;
    }

    @Override
    public void computeScroll() {
        if(mDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 初始化状态监听
     */
    private void initStateChangeListener() {
        mStateChangeListenerList = new ArrayList<>();
    }

    /**
     * 添加状态改变的监听
     *
     * @param listener
     */
    public void addStateChangeListener(StateChangeListener listener) {
        mStateChangeListenerList.add(listener);
    }

    /**
     * 移除状态改变监听
     *
     * @param listener
     */
    public void removeStateChangeListener(StateChangeListener listener) {
        mStateChangeListenerList.remove(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }


    private float downX;
    private float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        mDragHelper.processTouchEvent(event);

            switch (MotionEventCompat.getActionMasked(event)) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    if(mState == STATE_EXPANDED){
                        TagView tagView = findTopTagViewUnder(downX,downY);
                        if(tagView != null){
                            mDragHelper.captureChildView(tagView,event.getPointerId(0));
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mSlideEnable&&!isCaptured) {
                        float x = event.getX();
                        float y = event.getY();
                        if (Math.abs(downX - x) < 10) {
                            float dy = y - downY;
                            if (dy > 0 && dy > mSlideLength) {
                                expand();
                                return false;
                            }
                            if (dy < 0 && -dy > mSlideLength) {
                                collapse();
                                return false;
                            }
                        }
                    }
                    break;
            }
        return true;
    }

    public TagView findTopTagViewUnder(float x,float y){
        for(int i=0;i<getChildCount();i++){
            View child = getChildAt(i);
            if(child instanceof TagView){
                if(x>=child.getX()&&x<=child.getX()+child.getWidth()&&y>=child.getY()&&y<=child.getY()+child.getHeight()){
                    return (TagView) child;
                }
            }
        }
        return null;
    }

    /**
     * 返回当前状态 状态类型如下:
     * {@link #STATE_COLLAPSED},{@link #STATE_COLLAPSING},
     * {@link #STATE_EXPANDED},{@link #STATE_COLLAPSING}
     *
     * @return
     */
    public int getState() {
        return mState;
    }

    /**
     * 进行扩展动画 状态变化 {@link #STATE_EXPANDING}->{@link #STATE_EXPANDED}
     */
    public void expand() {
        if (mState == STATE_COLLAPSED) {
            if (mExpandAnimator == null || mCollapsedHeight == 0) {
                mExpandAnimator = createExpandAnimator();
                mExpandAnimator.addListener(mExpandListener);
            }
            mExpandAnimator.start();
        }
    }

    /**
     * 进行折叠动画 状态变化 {@link #STATE_COLLAPSING}->{@link #STATE_COLLAPSED}
     */
    public void collapse() {
        if (mState == STATE_EXPANDED) {
            if (mCollapsedAnimator == null || mCollapsedHeight == 0) {
                mCollapsedAnimator = createCollapseAnimator();
            }
            mCollapsedAnimator.start();
        }
    }


    private AnimatorSet createExpandAnimator() {
        ensureHeightIsCorrect();
        ObjectAnimator step1 = ObjectAnimator.ofInt(this, "height", mCollapsedHeight, mExpandHeight);
        step1.setDuration(400);
        ObjectAnimator step2 = ObjectAnimator.ofFloat(mBackgroundOverlay, "alpha", 0f, 1f);
        step2.setDuration(400);
        step2.addListener(mBlurOverlayExpandListener);
        ensurePortrait();
        Animator step3 = mPortrait.showHalo();
        Animator step4 = createTagAnimator(false);
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder builder = set.play(step1).with(step2);
        if (step3 != null) builder.before(step3);
        if (step4 != null) {
            step4.setStartDelay(600);
            builder.before(step4);
        }
        return set;
    }

    private AnimatorSet createCollapseAnimator() {
        ensureHeightIsCorrect();
        ObjectAnimator step1 = ObjectAnimator.ofInt(this, "height", mCollapsedHeight);
        ObjectAnimator step2 = ObjectAnimator.ofFloat(mBackgroundOverlay, "alpha", 1f, 0f);
        step1.setDuration(600);
        step2.setDuration(300);
        ensurePortrait();
        Animator step3 = mPortrait.hideHalo();
        if (step3 != null) step3.setDuration(300);
        Animator step4 = createTagAnimator(true);
        if (step4 != null) step4.setDuration(600);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(step1, step2, step3, step4);
        set.addListener(mCollapseListener);
        return set;
    }


    private Animator createTagAnimator(boolean reverse) {
        ensurePortrait();
        int centerX = mPortrait.getLeft() + mPortrait.getWidth() / 2;
        int expectHeight = reverse ? mCollapsedHeight : mExpandHeight;
        int foreCastTop = forecastTop((LayoutParams) mPortrait.getLayoutParams(), expectHeight, mPortrait.getHeight());
        if (foreCastTop == Integer.MIN_VALUE) foreCastTop = mPortrait.getTop();
        int centerY = foreCastTop + mPortrait.getHeight() / 2;
        if (mTagViews != null) {
            AnimatorSet animatorSet = new AnimatorSet();
            List<Animator> animators = new ArrayList<>();
            for (int i = 0; i < mTagViews.size(); i++) {
                TagView tagView = mTagViews.get(i);
                tagView.setVisibility(VISIBLE);
                float targetX, targetY, controlX, controlY;
                if (reverse) {
                    targetX = tagView.getX() + tagView.getWidth() / 2;
                    targetY = tagView.getY() + tagView.getHeight() / 2;
                    //将centerX 和 centerY 看做坐标原点
                    float x = targetX - centerX;
                    float y = targetY - centerY;
                    float scaledX = 0.3f * x;
                    float scaledY = 0.3f * y;
                    //计算控制点 保证bezier曲线的导数相同
                    controlX = centerX + Math.round(scaledX * Math.cos(CONTROL_RADIANS_OFFSET) + scaledY * Math.sin(CONTROL_RADIANS_OFFSET));
                    controlY = centerY + Math.round(scaledY * Math.cos(CONTROL_RADIANS_OFFSET) - scaledX * Math.sin(CONTROL_RADIANS_OFFSET));
                } else {
                    targetX = Math.round(centerX + (mInnerRadius + mThickness / 2) * Math.cos(RADIUS[i]));
                    targetY = Math.round(centerY - (mInnerRadius + mThickness / 2) * Math.sin(RADIUS[i]));
                    mTargets[2*i] = targetX;
                    mTargets[2*i+1] = targetY;
                    controlX = Math.round(centerX + mInnerRadius * Math.cos(RADIUS[i] + CONTROL_RADIANS_OFFSET));
                    controlY = Math.round(centerY - mInnerRadius * Math.sin(RADIUS[i] + CONTROL_RADIANS_OFFSET));
                }
                Path path = new Path();
                if (reverse) {
                    path.moveTo(targetX, targetY);
                    path.quadTo(controlX, controlY, centerX, centerY);
                } else {
                    path.moveTo(centerX, centerY);
                    path.quadTo(controlX, controlY, targetX, targetY);
                }
                ObjectAnimator pathAnimator;
                if (Build.VERSION.SDK_INT >= 21) {
                    pathAnimator = ObjectAnimator.ofObject(tagView, mTagViewProperty, null, path);
                } else {
                    pathAnimator = ObjectAnimator.ofObject(tagView, mTagViewProperty, new PathEvaluator(path), new PointF());
                }
                ObjectAnimator rotationAnimator;
                ObjectAnimator scaleXAnimator;
                ObjectAnimator scaleYAnimator;
                if (reverse) {
                    rotationAnimator = ObjectAnimator.ofFloat(tagView, "rotation", 0, -60);
                    scaleXAnimator = ObjectAnimator.ofFloat(tagView, "scaleX", 0);
                    scaleYAnimator = ObjectAnimator.ofFloat(tagView, "scaleY", 0);
                } else {
                    rotationAnimator = ObjectAnimator.ofFloat(tagView, "rotation", -60, 0);
                    scaleXAnimator = ObjectAnimator.ofFloat(tagView, "scaleX", 0, 1f);
                    scaleYAnimator = ObjectAnimator.ofFloat(tagView, "scaleY", 0, 1f);
                }

                animators.add(pathAnimator);
                animators.add(scaleXAnimator);
                animators.add(scaleYAnimator);
                animators.add(rotationAnimator);

            }
            animatorSet.playTogether(animators);
            animatorSet.setDuration(800);
            if (reverse) {
                animatorSet.setInterpolator(new AccelerateInterpolator());
            } else {
                animatorSet.setInterpolator(new DecelerateInterpolator());
            }
            return animatorSet;
        }
        return null;
    }


    /**
     * 预计宽度改变所造成的子view left将要变化到的值
     *
     * @param childParams
     * @param myWidth     parent width
     * @return
     */
    private int forecastLeft(LayoutParams childParams, int myWidth, int childWidth) {
        final int[] rules = childParams.getRules();
        RelativeLayout.LayoutParams anchorParams;
        int left = Integer.MIN_VALUE;
        anchorParams = invokeGetRelatedViewParams(rules, RIGHT_OF);
        if (anchorParams != null) {
            left = getFieldIntValue(anchorParams, "mRight") + (anchorParams.rightMargin +
                    childParams.leftMargin);
        } else if (childParams.alignWithParent && rules[RIGHT_OF] != 0) {
            left = getPaddingLeft() + childParams.leftMargin;
        }
        anchorParams = invokeGetRelatedViewParams(rules, ALIGN_LEFT);
        if (anchorParams != null) {
            left = getFieldIntValue(anchorParams, "mLeft") + childParams.leftMargin;
        } else if (childParams.alignWithParent && rules[ALIGN_LEFT] != 0) {
            left = getPaddingLeft() + childParams.leftMargin;
        }
        if (0 != rules[ALIGN_PARENT_LEFT]) {
            left = getPaddingLeft() + childParams.leftMargin;
        }
        if (rules[CENTER_HORIZONTAL] != 0 || rules[CENTER_IN_PARENT] != 0) {
            left = (myWidth - childWidth) / 2;
        }
        return left;
    }

    /**
     * 预计高度改变 所造成 子view top 将要改变到的值
     *
     * @param childParams
     * @param myHeight    parent height
     * @return
     */
    private int forecastTop(LayoutParams childParams, int myHeight, int childHeight) {
        final int[] rules = childParams.getRules();
        RelativeLayout.LayoutParams anchorParams;
        int top = Integer.MIN_VALUE;
        anchorParams = invokeGetRelatedViewParams(rules, BELOW);
        if (anchorParams != null) {
            top = getFieldIntValue(anchorParams, "mBottom") + (anchorParams.bottomMargin +
                    childParams.topMargin);
        } else if (childParams.alignWithParent && rules[BELOW] != 0) {
            top = getPaddingTop() + childParams.topMargin;
        }
        anchorParams = invokeGetRelatedViewParams(rules, ALIGN_TOP);
        if (anchorParams != null) {
            top = getFieldIntValue(anchorParams, "mTop") + childParams.topMargin;
        } else if (childParams.alignWithParent && rules[ALIGN_TOP] != 0) {
            top = getPaddingTop() + childParams.topMargin;
        }
        if (0 != rules[ALIGN_PARENT_TOP]) {
            top = getPaddingTop() + childParams.topMargin;
        }

        if (rules[CENTER_VERTICAL] != 0 || rules[CENTER_IN_PARENT] != 0) {
            top = (myHeight - childHeight) / 2;
        }
        return top;
    }

    /**
     * 反射调用{@link RelativeLayout#getRelatedViewParams}
     *
     * @param rules
     * @param relation
     * @return
     */
    private LayoutParams invokeGetRelatedViewParams(int[] rules, int relation) {
        try {
            Method method = RelativeLayout.class.getDeclaredMethod("getRelatedViewParams", int[].class, int.class);
            method.setAccessible(true);
            return (LayoutParams) method.invoke(this, rules, relation);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反射获取value{@link android.widget.RelativeLayout.LayoutParams}
     *
     * @param layoutParams
     * @param fieldName
     * @return
     */
    private int getFieldIntValue(LayoutParams layoutParams, String fieldName) {
        try {
            Field field = layoutParams.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (int) field.get(layoutParams);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return Integer.MIN_VALUE;
    }

    /**
     * 模糊背景操作任务
     */
    private Runnable mDoBlurRunnable = new Runnable() {
        @Override
        public void run() {
            Bitmap bitmap = Util.getBitmapFromDrawable(getBackground());
            if (mOriginBackground == null) mOriginBackground = getBackground();
            if (bitmap != null) {
                Bitmap blurBitmap = Util.doBlur(getContext(), bitmap, 10);
                Drawable newDrawable = new BitmapDrawable(getResources(), blurBitmap);
                mBluredBackground = newDrawable;
                Util.setBackground(mBackgroundOverlay, newDrawable);
            }
        }
    };
    /**
     * 做tag 周围小幅度移动动画
     */
    private Runnable mTagWanderRunnable = new Runnable() {
        @Override
        public void run() {
            if (mState == STATE_EXPANDED) {
                for (int i = 0; i < mTagViews.size(); i++) {
                    TagView tagView = mTagViews.get(i);
                    if (tagView.shouldWander) {
                        float targetX = mTargets[2*i] - tagView.getWidth()/2;
                        float targetY = mTargets[2*i+1] - tagView.getHeight()/2;
                        float x = Math.round(targetX + Math.random()*mThickness - mThickness/2);
                        float y = Math.round(targetY + Math.random()*mThickness - mThickness/2);
                        tagView.animate().translationX(x).translationY(y).setDuration(2000);
                    }
                }
                ViewCompat.postOnAnimationDelayed(PresentationLayout.this, mTagWanderRunnable, 2000);
            }
        }
    };

    private void ensureHeightIsCorrect() {
        if (mCollapsedHeight == 0) {
            mCollapsedHeight = getHeight();
        }
        if (mExpandHeight == 0) {
            if (getParent() instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) getParent();
                mExpandHeight = viewGroup.getHeight();
            } else {
                mExpandHeight = mCollapsedHeight;
            }
        }
    }

    public void setHeight(int height) {
        getLayoutParams().height = height;
        if (Build.VERSION.SDK_INT >= 18 && !isInLayout()) {
            requestLayout();
        } else {
            requestLayout();
        }
        invalidate();
    }


    public void autoControl() {
        if (mState == STATE_COLLAPSED) {
            expand();
        }
        if (mState == STATE_EXPANDED) {
            collapse();
        }
       /* if(mState == STATE_COLLAPSING){
            mState = STATE_COLLAPSED;
            ViewCompat.postInvalidateOnAnimation(this);
            Log.w(TAG,"状态STATE_COLLAPSING强制转换为STATE_COLLAPSED");
        }
        if(mState == STATE_EXPANDING){
            mState = STATE_EXPANDED;
            ViewCompat.postInvalidateOnAnimation(this);
            Log.w(TAG,"状态STATE_EXPANDING强制转换为STATE_EXPANDED");
        }*/
    }


    private void ensurePortrait() {
        View view = this.findViewById(R.id.portrait);
        if (view != null && view instanceof Portrait) {
            mPortrait = (Portrait) view;
        } else {
            throw new IllegalArgumentException("Require contain the view implement Portrait.java and set id is R.id.portrait");
        }
    }

    /**
     * 添加所有标签
     *
     * @param tags
     */
    public void inputTags(List<Tag> tags) {
        if (tags == null || tags.size() == 0) return;
        if (mTagViews != null) {
            mTagViews.clear();
        }
        for (int i = 0; i < tags.size(); i++) {
            if (i > 7) {
                Log.w(TAG, "最多设置7个标签");
                break;
            }
            inputTag(tags.get(i));
        }
        for (View child : mTagViews) {
            addViewInLayout(child, -1, generateDefaultLayoutParams());
        }
        requestLayout();
        invalidate();
    }

    /**
     * 添加单个标签
     *
     * @param tag
     */
    public void inputTag(Tag tag) {
        if (tag == null) return;
        if (mTagViews == null) {
            mTagViews = new ArrayList<>();
        }
        if (mTagViews.size() < 7) {
            TagView tagView = new TagView(getContext());
            tagView.initOriginPadding(mTagPadding, mTagPadding, mTagPadding, mTagPadding);
            tagView.innerInit(mTagViewTextColor,mTagViewTextSize,mTagViewBackgroundColor,mTagViewBorderColor,mTagViewBorderWidth);
            tagView.setSource(tag);
            tagView.setVisibility(GONE);
            tagView.setScaleX(0);
            tagView.setScaleY(0);
            mTagViews.add(tagView);
        }
    }

    /**
     * 标签holder
     */
    public static class Tag {
        /**
         * tag 文字显示主体
         */
        String tag = "";
        /**
         * 数量
         */
        int count = 1;

        public Tag(String tag, int count) {
            this.tag = tag;
            this.count = count;
        }

        public static Tag newTag(String tag, int count) {
            return new Tag(tag, count);
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getTag() {
            return tag;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * 监听 扩展动画
     */
    private AnimatorListenerAdapter mExpandListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            mState = STATE_EXPANDING;
            doStateChange(mState);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mState = STATE_EXPANDED;
            doStateChange(mState);
            ViewCompat.postOnAnimation(PresentationLayout.this, mTagWanderRunnable);
        }
    };
    private AnimatorListenerAdapter mBlurOverlayExpandListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            mBackgroundOverlay.setVisibility(VISIBLE);
        }
    };

    private AnimatorListenerAdapter mCollapseListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            mState = STATE_COLLAPSING;
            doStateChange(mState);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mBackgroundOverlay.setVisibility(GONE);
            mState = STATE_COLLAPSED;
            doStateChange(mState);
        }
    };

    private void doStateChange(int state) {
        for (StateChangeListener stateChangeListener : mStateChangeListenerList) {
            stateChangeListener.onStateChange(state);
        }
    }

    public interface StateChangeListener {
        void onStateChange(int state);
    }
}
