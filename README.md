# AutoInject
结合Transform＋ASM，在编译期进行代码注入。

## Usage
在根项目的build.gradle中添加插件依赖：

    buildscript {
        ... 
        dependencies {
            ...
            classpath 'com.eastwood.tools.plugins:auto-inject:1.0.0'
        }
        
    }

继续在模块build.gradle中添加注解库依赖：

    dependencies {
        ...
        implementation 'com.eastwood.common:auto-inject:1.0.0'
         
    }

#### @AutoTarget
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

`addIndex2EventBus` 方法将被注入代码。

#### @AutoArrow
新建一个类，并实现IAutoArrow接口，在get方法中返回需要被注册的信息类。例如：

    @AutoArrow(model = "eventBusIndex")
    public class ModuleBAutoArrow implements IAutoArrow<SubscriberInfoIndex> {
     
        @Override
        public SubscriberInfoIndex get() {
            return new ModuleBEventBusIndex();
        }
     
    }

#### @AutoBow
新建一个类，并实现IAutoBow接口，在shoot方法中获取入参并执行具体的注册逻辑。例如：

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

其中 `context` 用于声明EventBusAutoBow被实例化时是否需要上下文，这个上下文是被`@AutoTarget`标记的方法在运行时的上下文。为 `true`时，该类需定义一个以上下文做为唯一入参的构造函数。

#### @AutoBowArrow
新建一个类，并实现IAutoBowArrow接口，在shoot方法中执行相关逻辑。

    @AutoBowArrow(target = "init")
    public class InitAutoBowArrow implements IAutoBowArrow {

        @Override
        public void shoot() {
            // ...
        }

    }
    
### 两种组合方式
* @AutoArrow + @AutoBow + @AutoTarget，三者关系为 **n:1:1**

* @AutoBowArrow + @AutoTarget ，两者比例关系为 **1:1**

### 编译后，被注入的代码样式
打包成apk后，@AutoTarget标记的方法将会被注入具有固定结构的代码，例如：


    // @AutoArrow + @AutoBow + @AutoTarget 组合
     
    @AutoTarget
    void addIndex2EventBus() {
        ModuleBAutoArrow moduleBAutoArrow = new ModuleBAutoArrow();
        EventBusAutoBow eventBusAutoBow = new EventBusAutoBow(this);
        eventBusAutoBow.shoot(moduleBAutoArrow.get());
        ...
        eventBusAutoBow.shoot(***.get());
    }
     
    // @AutoBowArrow + @AutoTarget 组合
     
    @AutoTarget
    void init() {
        InitAutoBowArrow initAutoBowArrow = new InitAutoBowArrow();
        initAutoBowArrow.shoot();
        ...
    }