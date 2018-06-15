package com.winwin.lab.rounter.moduleb;

import com.eastwood.common.autoinject.AutoArrow;
import com.eastwood.common.autoinject.IAutoArrow;

@AutoArrow(model = "toast")
public class ModuleBToastAutoArrow implements IAutoArrow<String> {

    @Override
    public String get() {
        return "This is module B.";
    }
}
