# AutoRegister

#### 在编译期，通过收集相关被标记的类，注入到被标记的方法中，自动初始化并执行被标记的类中的方法。

适用的业务场景：

* 跨模块公共库的初始化，需要初始化的信息分散在不同模块中，比如Router。


## Usage

### @AutoTarget
用于标记要注入代码的位置，只能标记在方法上。

    // name 表示目标位置的名称，需自定义
    @AutoTarget(name = "routerInit")
    void routerInit() {}

    // 可不填name，默认取方法名称。
    @AutoTarget
    void routerInit() {}

    // 可定义多个别名
    @AutoTarget(name = {"routerInit", "***Init"})
    void init() {}

### @AutoArrow
用于获取需要被注入的信息。
被标记的类需要实现IAutoArrow接口，并在get方法中返回需要被注入的信息。


    // model 表示被注入的信息的类型，需自定义
    @AutoArrow(model = "router")
    public class RouterAutoArrow implements IAutoArrow<RouterInfoIndex> {

        @Override
        public RouterInfoIndex get() {
            return new ModuleRouterIndex();
        }
    }


### @AutoBow
用于适配目标位置和被注入信息的类型。
被标记的类需要实现IAutoBow接口，并在shoot方法中执行相关动作。

    // target 表示目标位置的名称
    // model  表示被注入的信息的类型
    @AutoBow(target = "routerInit", model = "router")
    public class RouterAutoBow implements IAutoBow<RouterInfoIndex> {

        @Override
        public void shoot(RouterInfoIndex routerInfoIndex) {
            Router.addRouterIndex(routerInfoIndex);
        }

    }

### @AutoBowTarget
被标记的类需要实现IAutoBowArrow接口，并在shoot方法中执行相关动作。

    // target 表示目标位置的名称
    // model  表示被注入的信息的类型
    @AutoBowArrow(target = "init")
    public class InitAutoBowArrow implements IAutoBowArrow {

        @Override
        public void shoot() {
            // ...
        }

    }

### 自动初始化并执行

比如将被注入的代码如下：

    @AutoTarget
    void routerInit() {
        RouterAutoBow routerAutoBow = new RouterAutoBow();
        RouterAutoArrow routerAutoArrow = new RouterAutoArrow();
        routerAutoBow.shoot(routerAutoArrow.get());
    }

    @AutoTarget
    void init() {
        InitAutoBowArrow initAutoBowArrow = new InitAutoBowArrow();
        initAutoBowArrow.shoot();
    }

