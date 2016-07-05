package com.anarchy.qqpresentation.presentation;

import android.graphics.PointF;
import android.util.Property;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/4 15:16
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 */
class TagViewProperty extends Property<TagView,PointF>{

    /**
     * A constructor that takes an identifying name and {@link #getType() type} for the property.
     *
     * @param type
     * @param name
     */
    public TagViewProperty(Class<PointF> type, String name) {
        super(type, name);
    }

    @Override
    public PointF get(TagView tagView) {
        return tagView.getPoint();
    }

    @Override
    public void set(TagView tagView, PointF value) {
        tagView.setPoint(value);
    }
}
