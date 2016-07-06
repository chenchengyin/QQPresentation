package com.anarchy.qqpresentation.presentation;

import android.animation.Animator;
import android.view.ViewGroup;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/1 15:49
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 * Copyright © 2014-2016 Shanghai Xiaotu Network Technology Co., Ltd.
 */
public interface Portrait {
    Animator showHalo();
    Animator hideHalo();
    void want(boolean want);
    void desire(boolean desire);
    int getLeft();
    int getTop();
    int getWidth();
    int getHeight();
    float getX();
    float getY();
    ViewGroup.LayoutParams getLayoutParams();
    int getDesireLeft();
    int getDesireRight();
    int getDesireTop();
    int getDesireBottom();
}
