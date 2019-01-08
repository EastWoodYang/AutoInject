# AutoInject
Android 通用的组件自动注册、自动初始化解决方案

## 背景问题
我们在组件化的过程，业务被拆分至独立的Module中，一些公用组件会在各个Module中通过APT生成一些需要被注册至组件中的信息类，比如EventBus生成的Index类。我们这边RN定制的Plugin是跟随各自module，需要被注册。还有，各Module对外提供的api接口的话，也需要被注册。

另外，有些组件为某些Module特有，需要在App启动的时候就要初始化，有些需要在主线程中初始化，有些为不阻塞主线程可以在非主线程中初始化。

在组件化之前，我们是在Main Module通过硬编码来进行注册，在Application中堆积各个组件的初始化逻辑。

**有没有更好的解决方式？**

## 解决思路
首先，将问题分解抽象：

* 把注册行为进行抽象化，可以把一个类（需要被注册的信息）看作方法函数的入参，那方法函数就可以看作是对注册相关逻辑的实现。那注册问题可以进一步转化为各模块如何把相关类（需要被注册的信息）转化为方法函数的入参，组件定义方法函数，获取入参来实现注册逻辑。比如：

        
        A a = new A()
        B b = new B()
        b.shoot(a.get())
        
        A为Module定义的一个类，通过get方法可以获得被注册的信息，B为组件定义的一个类，在shoot方法中实现注册逻辑。
    
* 接下来的问题是，AB分处不同模块，如何把AB按上述代码逻辑组合起来？
    
    AB组合意味着需要代码注入，代码注入使用的技术方案是Gradle Transform + ASM。AB分别实现约定的接口，再用注解标记。在编译时，通过Gradle Transform + ASM，通过注解找到AB，生成上述格式的代码并注入到合适的位置。

* 剩下的问题就是，在什么位置注入？
    
    采用的方式是预先定义一个空方法，通过注解标记，并在适当时机调用这个空方法。在编译时通过注解找到AB和空方法，生成上述格式的代码并注入到这个空方法中。

以上是有关组件注册方面的解决思路，而模块中的组件初始化有点不同，因为其不需要入参。 但可以直接在方法函数中实现初始化逻辑，比如：
    
        
        A a = new A()
        a.shoot()
        
        模块定义一个类A，实现约定的接口，在shoot方法中做实现初始化逻辑。
        

其余和组件注册方式相类似，主要在注入的代码逻辑上有所不同。


## 设计模型
<img src='https://user-gold-cdn.xitu.io/2018/6/20/1641b530b2ec3d1a?w=404&h=313&f=png&s=45018'/>

具体的实现方式其实是借鉴了弓箭耙的模式。

弓箭耙模式: 
* 箭 Arrow：对应一种型号

    提供模块相关类（需要被注册的信息）的载体。
    
* 弓 Bow：适配一种型号的箭，射向唯一的耙。
    
    方法函数，即实现注册逻辑的载体。

* 耙 Target：位置
    
    将被注入的空方法。

## Usage
在根项目的build.gradle中添加插件依赖：

    buildscript {
        ... 
        dependencies {
            ...
            classpath 'com.eastwood.tools.plugins:auto-inject:1.0.3'
        }
        
    }

在模块的build.gradle中添加注解库依赖：

    dependencies {
        ...
        implementation 'com.eastwood.common:auto-inject:1.0.0'
         
    }
    
在主模块的build.gradle中引用插件：
    
    
    apply plugin: 'auto-inject'
     
    autoInject {
        showLog = true
        ignorePackages = ['android', 'com/google']
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
* @AutoArrow + @AutoBow + @AutoTarget，三者比例关系为 **n:1:1**

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
