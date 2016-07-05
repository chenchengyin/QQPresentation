package com.anarchy.qqpresentation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.anarchy.qqpresentation.presentation.PresentationLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String[] TAGS= new String[]{"托比昂","死神","麦克雷","黑百合","路霸","狂鼠","士兵76"};//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PresentationLayout presentationLayout = (PresentationLayout) findViewById(R.id.presentation);
        List<PresentationLayout.Tag> mSource = new ArrayList<>();
        for(String t:TAGS){
            PresentationLayout.Tag tag = new PresentationLayout.Tag(t,0);
            mSource.add(tag);
        }
        presentationLayout.inputTags(mSource);
        presentationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentationLayout.autoControl();
            }
        });
    }
}
