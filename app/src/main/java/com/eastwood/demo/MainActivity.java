package com.eastwood.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.eastwood.common.autoinject.AutoTarget;

/**
 * @author eastwood
 * createDate: 2018-09-18
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_show_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast();
            }
        });

    }

    @AutoTarget(name = "toast")
    void showToast() {

    }

}