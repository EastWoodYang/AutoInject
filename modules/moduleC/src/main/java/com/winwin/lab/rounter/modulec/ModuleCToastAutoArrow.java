package com.winwin.lab.rounter.modulec;

import com.eastwood.common.autoinject.AutoArrow;
import com.eastwood.common.autoinject.IAutoArrow;

@AutoArrow(model = "toast")
public class ModuleCToastAutoArrow implements IAutoArrow<String> {

    @Override
    public String get() {
        return "This is module C.";
    }
}
