package com.anarchy.qqpresentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.anarchy.qqpresentation.presentation.PresentationLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PresentationLayout presentationLayout = (PresentationLayout) findViewById(R.id.presentation);
        presentationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentationLayout.autoControl();
            }
        });
    }
}
