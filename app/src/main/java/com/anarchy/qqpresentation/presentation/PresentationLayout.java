package com.anarchy.qqpresentation.presentation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.anarchy.qqpresentation.R;
import com.anarchy.qqpresentation.presentation.utils.Util;

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
    private static final int STATE_COLLAPSED = 0;
    private static final int STATE_EXPANDING = 1;
    private static final int STATE_EXPANDED = 2;
    private static final int STATE_COLLAPSING = 3;
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

    private List<TagView> mTagViews;

    public PresentationLayout(Context context) {
        super(context);
        init(context);
    }

    public PresentationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PresentationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mBackgroundOverlay = new View(context);
        addViewInLayout(mBackgroundOverlay,0,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child instanceof TagView;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
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
            }
        }
    }

    private AnimatorSet createExpandAnimator() {
        ensureHeightIsCorrect();
        ObjectAnimator step1 = ObjectAnimator.ofInt(this,"height",mCollapsedHeight,mExpandHeight);
        step1.setDuration(400);
        ObjectAnimator step2 = ObjectAnimator.ofFloat(mBackgroundOverlay,"alpha",0f,1f);
        step2.setDuration(400);
        ensurePortrait();
        Animator step3 = mPortrait.showHalo();
        Animator step4 = createExpandTagAnimator();
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder builder = set.play(step1).with(step2);
        if(step3 != null) builder.before(step3);
        if(step4 != null) builder.before(step4);
        return set;
    }


    private Animator createExpandTagAnimator(){
        if(mTagViews != null){
        }
        return null;
    }


    private Runnable mDoBlurRunnable = new Runnable() {
        @Override
        public void run() {
           Bitmap bitmap =  Util.getBitmapFromDrawable(getBackground());
            if(mOriginBackground == null) mOriginBackground = getBackground();
            if(bitmap != null){
                Bitmap blurBitmap = Util.doBlur(getContext(),bitmap,20);
                Drawable newDrawable = new BitmapDrawable(getResources(),blurBitmap);
                mBluredBackground = newDrawable;
                Util.setBackground(mBackgroundOverlay,newDrawable);
            }
        }
    };
    private void ensureHeightIsCorrect(){
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


    /**
     * 进行折叠动画 状态变化 {@link #STATE_COLLAPSING}->{@link #STATE_COLLAPSED}
     */
    public void collapse() {
        if (mState == STATE_EXPANDED) {

        }
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
            throw new IllegalArgumentException("Require add the view implement Portrait.java and set id is R.id.portrait");
        }
    }

    /**
     * 添加所有标签
     *
     * @param tags
     */
    public void inputTags(List<Tag> tags) {
        if(tags == null||tags.size() == 0) return;
        if (mTagViews != null) {
            mTagViews.clear();
        }
        for (Tag tag : tags) {
            inputTag(tag);
        }
        for(View child:mTagViews){
            addViewInLayout(child,-1,generateDefaultLayoutParams());
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
        if(tag == null) return;
        if (mTagViews == null) {
            mTagViews = new ArrayList<>();
        }
        TagView tagView = new TagView(getContext());
        tagView.setSource(tag);
        tagView.setVisibility(GONE);
        tagView.setScaleX(0);
        tagView.setScaleY(0);
        mTagViews.add(tagView);
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

        private Tag(String tag, int count) {
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
}