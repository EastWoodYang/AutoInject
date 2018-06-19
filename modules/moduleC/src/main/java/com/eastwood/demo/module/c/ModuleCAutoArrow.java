package com.eastwood.demo.module.c;

import com.eastwood.common.autoinject.AutoArrow;
import com.eastwood.common.autoinject.IAutoArrow;
import com.eastwood.demo.eventbus.ModuleCEventBusIndex;

import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

/**
 * @author eastwood
 * createDate: 2018-06-19
 */
@AutoArrow(model = "eventBusIndex")
public class ModuleCAutoArrow implements IAutoArrow<SubscriberInfoIndex> {

    @Override
    public SubscriberInfoIndex get() {
        return new ModuleCEventBusIndex();
    }

}
