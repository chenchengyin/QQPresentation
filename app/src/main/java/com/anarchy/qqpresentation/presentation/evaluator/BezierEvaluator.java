package com.anarchy.qqpresentation.presentation.evaluator;

import android.animation.TypeEvaluator;
import android.graphics.Point;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/7/4 09:41
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 * @deprecated use path animation
 */
public class BezierEvaluator implements TypeEvaluator<Point> {
    private Point[] mControlPoints;

    public static BezierEvaluator getInstance(Point... controlPoints) {
        return new BezierEvaluator(controlPoints);
    }

    BezierEvaluator(Point... controlPoints) {
        mControlPoints = controlPoints;
    }




    /**
     * 德卡斯特里奥算法Pi,j = (1 - u)Pi-1,j + uPi-1,j+1
     * index 0 1 2 3
     * 0     1
     *         5
     * 1     2   8
     *         6  10
     * 2     3   9
     *         7
     * 3     4
     * <p/>
     * 10 则为bezier曲线上的点 传入 i = 3  j = 0 则为 4阶bezier曲线上的点
     * 当i=0 时对应的是控制点
     *
     * @param fraction u 曲线比例
     * @param i        bezier 阶
     * @param j        控制点 index
     * @return
     */
    public float deCasteljauX(float fraction, int i, int j) {
        if(i==0){
            return mControlPoints[j].x;
        }else {
            return (1-fraction)*deCasteljauX(fraction,i-1,j)+fraction*deCasteljauX(fraction,i-1,j+1);
        }
    }


    public float deCasteljauY(float fraction,int i,int j){
        if(i == 0){
            return mControlPoints[j].y;
        }else {
            return (1-fraction)*deCasteljauY(fraction,i-1,j)+fraction*deCasteljauY(fraction,i-1,j+1);
        }
    }


    @Override
    public Point evaluate(float fraction, Point startValue, Point endValue) {
        int x = (int) deCasteljauX(fraction,mControlPoints.length-1,0);
        int y = (int) deCasteljauY(fraction,mControlPoints.length-1,0);
        return new Point(x,y);
    }
}