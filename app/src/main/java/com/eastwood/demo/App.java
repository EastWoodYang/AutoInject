package com.eastwood.demo;

import android.app.Application;

import com.eastwood.common.autoinject.AutoTarget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusBuilder;

/**
 * @author eastwood
 * createDate: 2018-06-19
 */
public class App extends Application {

    public EventBusBuilder eventBusBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        eventBusBuilder = EventBus.builder();
        // add config to eventBusBuilder
        addIndex2EventBus();
        eventBusBuilder.build();

    }

    @AutoTarget
    void addIndex2EventBus() {
    }

}
