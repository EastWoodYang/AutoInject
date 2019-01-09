package com.eastwood.demo;

import android.content.Context;
import android.widget.Toast;

import com.eastwood.common.autoinject.AutoBow;
import com.eastwood.common.autoinject.AutoBowArrow;
import com.eastwood.common.autoinject.IAutoBow;
import com.eastwood.common.autoinject.IAutoBowArrow;

@AutoBowArrow(target = "toast", context = true)
public class ToastAutoBow implements IAutoBowArrow {

    private Context context;

    ToastAutoBow(Context context) {
        this.context = context;
    }

    @Override
    public void shoot() {
        Toast.makeText(context, "Hi", Toast.LENGTH_LONG).show();
    }
}
