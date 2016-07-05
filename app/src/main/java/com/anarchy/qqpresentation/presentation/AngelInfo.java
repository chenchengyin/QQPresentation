package com.anarchy.qqpresentation.presentation;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/4 11:37
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 * Copyright © 2014-2016 Shanghai Xiaotu Network Technology Co., Ltd.
 */
public class AngelInfo {
    private double mAngel;
    private double mRadius;
    private int mX;
    private int mY;

    public AngelInfo(double angel, double radius) {
        mAngel = angel;
        mRadius = radius;
    }

    public void set(double angel,double radius){
        mAngel = angel;
        mRadius = radius;
    }

    public double getAngel() {
        return mAngel;
    }

    public void setAngel(double angel) {
        mAngel = angel;
    }

    public double getRadius() {
        return mRadius;
    }

    public void setRadius(double radius) {
        mRadius = radius;
    }
}
