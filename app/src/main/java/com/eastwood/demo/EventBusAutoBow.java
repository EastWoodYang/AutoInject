package com.eastwood.demo;

import android.app.Application;

import com.eastwood.common.autoinject.AutoBow;
import com.eastwood.common.autoinject.IAutoBow;

import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

@AutoBow(target = "addIndex2EventBus", model = "eventBusIndex", context = true)
public class EventBusAutoBow implements IAutoBow<SubscriberInfoIndex> {

    private App app;

    EventBusAutoBow(Application application) {
        app = (App) application;
    }

    @Override
    public void shoot(SubscriberInfoIndex index) {
        app.eventBusBuilder.addIndex(index);
    }

}
