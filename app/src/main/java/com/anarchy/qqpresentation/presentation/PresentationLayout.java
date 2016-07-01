package com.anarchy.qqpresentation.presentation;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.anarchy.qqpresentation.R;

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


    private List<TagView> mTagViews;

    public PresentationLayout(Context context) {
        super(context);
        init();
    }

    public PresentationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PresentationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
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

        }
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
     * @param tags
     */
    public void inputTags(List<Tag> tags) {
        if (mTagViews != null) {
            mTagViews.clear();
        }
        for (Tag tag : tags) {
            inputTag(tag);
        }
    }

    /**
     * 添加单个标签
     * @param tag
     */
    public void inputTag(Tag tag){
        if(mTagViews == null){
            mTagViews = new ArrayList<>();
        }
        TagView tagView = new TagView(getContext());
        tagView.setSource(tag);
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
