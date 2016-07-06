package com.anarchy.qqpresentation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.anarchy.qqpresentation.presentation.PresentationLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String[] TAGS= new String[]{"托比昂","死神","麦克雷","黑百合","路霸","狂鼠","士兵76"};//
    private PresentationLayout mPresentationLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresentationLayout = (PresentationLayout) findViewById(R.id.presentation);
        List<PresentationLayout.Tag> mSource = new ArrayList<>();
        for(String t:TAGS){
            PresentationLayout.Tag tag = new PresentationLayout.Tag(t,0);
            mSource.add(tag);
        }
        mPresentationLayout.inputTags(mSource);
    }
}
