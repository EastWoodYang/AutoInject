package com.winwin.lab.rounter.modulec;

import android.content.Context;
import android.widget.Toast;

import com.eastwood.common.autoinject.AutoBowArrow;
import com.eastwood.common.autoinject.IAutoBowArrow;

@AutoBowArrow(target = "toast-c", context = true)
public class ModuleCToastAutoBowArrow implements IAutoBowArrow {

    private Context context;

    ModuleCToastAutoBowArrow(Context context) {
        this.context = context;
    }

    @Override
    public void shoot() {

        Toast.makeText(context, "This is module C.", Toast.LENGTH_LONG).show();

    }


}
