package com.eastwood.demo.module.b;

import com.eastwood.common.autoinject.AutoArrow;
import com.eastwood.common.autoinject.IAutoArrow;
import com.eastwood.demo.eventbus.ModuleBEventBusIndex;

import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

/**
 * @author eastwood
 * createDate: 2018-06-19
 */
@AutoArrow(model = "eventBusIndex")
public class ModuleBAutoArrow implements IAutoArrow<SubscriberInfoIndex> {

    @Override
    public SubscriberInfoIndex get() {
        return new ModuleBEventBusIndex();
    }

}
