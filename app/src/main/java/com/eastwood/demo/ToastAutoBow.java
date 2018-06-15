package com.eastwood.demo;

import android.content.Context;
import android.widget.Toast;

import com.eastwood.common.autoinject.AutoBow;
import com.eastwood.common.autoinject.IAutoBow;

@AutoBow(target = "toast", model = "toast", context = true)
public class ToastAutoBow implements IAutoBow<String> {

    private Context context;

    ToastAutoBow(Context context) {
        this.context = context;
    }

    @Override
    public void shoot(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
