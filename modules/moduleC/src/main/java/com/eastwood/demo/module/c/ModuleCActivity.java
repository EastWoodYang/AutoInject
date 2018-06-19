package com.eastwood.demo.module.c;

import android.app.Activity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ModuleCActivity extends Activity {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String action) {
        Toast.makeText(this, action, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
