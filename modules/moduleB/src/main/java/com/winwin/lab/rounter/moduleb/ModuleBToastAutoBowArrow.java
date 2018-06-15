package com.winwin.lab.rounter.moduleb;

import android.content.Context;
import android.widget.Toast;

import com.eastwood.common.autoinject.AutoBowArrow;
import com.eastwood.common.autoinject.IAutoBowArrow;

@AutoBowArrow(target = "toast-c", context = true)
public class ModuleBToastAutoBowArrow implements IAutoBowArrow {

    private Context context;

    ModuleBToastAutoBowArrow(Context context) {
        this.context = context;
    }

    @Override
    public void shoot() {

        Toast.makeText(context, "This is module B.", Toast.LENGTH_LONG).show();

    }


}
