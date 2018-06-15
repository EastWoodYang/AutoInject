package com.eastwood.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.eastwood.common.autoinject.AutoTarget;

public class MainActivity extends Activity {

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

        findViewById(R.id.btn_show_b_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModuleBToast();
            }
        });

        findViewById(R.id.btn_show_c_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModuleCToast();
            }
        });

    }

    @AutoTarget(name = "toast")
    void showToast() {
    }

    @AutoTarget(name = "toast-b")
    void showModuleBToast() {
    }

    @AutoTarget(name = "toast-c")
    void showModuleCToast() {
    }
}
