# AutoInject
结合Transform＋ASM，在编译期进行代码注入。

## 设计原型
利用弓把对应型号的箭射向耙子。

Arror：提供对象。

Bow：获取对象，并执行相关动作。

Target：在什么位置执行。


<img src='https://github.com/EastWoodYang/AutoInject/blob/master/picture/1.png'/>


## Usage

Add buildscript dependency in root project build.gradle

    buildscript {
        
        ... 
        
        dependencies {
     
            ...
            classpath 'com.eastwood.tools.plugins:auto-inject:1.0.0'
            
        }
        
    }

and add dependency to module build.gradle

    dependencies {
     
        ...
        implementation 'com.eastwood.common:auto-inject:1.0.0'
         
    }

### @AutoArrow
为弓提供对象。

新建一个类，并实现IAutoArrow接口，在get方法中返回对象。例如：

    // model 表示类型
    @AutoArrow(model = "eventBusIndex")
    public class ModuleBAutoArrow implements IAutoArrow<SubscriberInfoIndex> {
     
        @Override
        public SubscriberInfoIndex get() {
            return new ModuleBEventBusIndex();
        }
     
    }

### @AutoBow
获取对象，并执行相关动作。

新建一个类，并实现IAutoBow接口，在shoot方法中获取对象并执行相关动作。例如：


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

### @AutoTarget
在什么位置执行。

预先定义一个空方法并调用，在方法上标记@AutoTarget，例如：


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
        void addIndex2EventBus() {}
    
    }

### @AutoBowArrow
直接在shoot方法中，执行相关动作。

新建一个类，并实现IAutoBowArrow接口，在shoot方法中执行相关动作。

    // target 表示位置的名称
    @AutoBowArrow(target = "init")
    public class InitAutoBowArrow implements IAutoBowArrow {

        @Override
        public void shoot() {
            // ...
        }

    }

## 被注入的代码
被注入的代码样式固定，例如：

    @AutoTarget
    void addIndex2EventBus() {
        ModuleBAutoArrow moduleBAutoArrow = new ModuleBAutoArrow();
        EventBusAutoBow eventBusAutoBow = new EventBusAutoBow();
        eventBusAutoBow.shoot(moduleBAutoArrow.get());
    }
     
    @AutoTarget
    void init() {
        InitAutoBowArrow initAutoBowArrow = new InitAutoBowArrow();
        initAutoBowArrow.shoot();
    }

