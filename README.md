# QQPresentation
仿QQ个人资料气泡展示
#效果展示
![](https://github.com/AlphaBoom/QQPresentation/raw/master/QQPresentation.gif)
#使用方式

```
 <com.anarchy.qqpresentation.presentation.PresentationLayout
        android:id="@+id/presentation"
        android:background="@drawable/bg"
        android:layout_width="match_parent"
        android:layout_height="350dp">
        <com.anarchy.qqpresentation.presentation.PortraitWrapper
            android:id="@id/portrait"
            app:SecondHaloColor="#96ffffff"
            app:FirstHaloColor="#a5ffffff"
            android:layout_centerInParent="true"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">
            <com.anarchy.qqpresentation.presentation.CircleImageView
                android:layout_width="90dp"
                android:layout_centerInParent="true"
                android:src="@drawable/yuan"
                android:layout_height="90dp"/>
        </com.anarchy.qqpresentation.presentation.PortraitWrapper>

    </com.anarchy.qqpresentation.presentation.PresentationLayout>
```
注意需要设置头像容器的id为 android:id="@id/portrait"

#支持的自定义属性设置

##PortraitWrapper

属性名|默认值|说明
---|---|----
BorderColor|#FFFFFF|外侧圆形边框的颜色
BorderWidth|1dp|外侧圆形边框的宽度
FirstHaloColor|#FAAFFFFF|内层光晕的颜色
SecondHaloColor|#CCCCFFFF|外层光晕的颜色
HaloTotalWidth|50dp|光晕总宽度用于计算Wrapper的大小

##PresentationLayout
属性名|默认值|说明
----|---|----
InnerRadius|130dp|用于计算控制点的半径
Thickness|20dp|这个数值的一般加上InnerRadius就是气泡表在所在位置，并且也是气泡标签不规则移动的范围
TagPadding|5dp| 标签文字的padding值
SlideEnable|true|是否支持滑动开启和折叠
SlideLength|5dp|判断滑动开启和折叠的触发的距离
TagViewTextColor|#FFFFFF| 标签的文字颜色
TagViewTextSize|15sp|标签的文字大小
TagViewBackgroundColor|#c0393d3d| 标签背景颜色
TagViewBorderWidth|1dp| 标签边框宽度
TagViewBorderColor|#e3a9a9a9| 标签边框颜色
