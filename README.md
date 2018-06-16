# AutoInject
结合Transform＋ASM，在编译期进行代码注入。

## 设计原型
利用弓把对应型号的箭射向耙子。

箭：提供对象。

弓：获取对象，并执行相关动作。

耙：在什么位置执行。

## Usage

### @AutoArrow
为弓提供对象。

新建一个类，并实现IAutoArrow接口，在get方法中返回对象。
    // model 表示类型
    @AutoArrow(model = "router")
    public class RouterAutoArrow implements IAutoArrow<RouterInfoIndex> {

        @Override
        public RouterInfoIndex get() {
            return new ModuleRouterIndex();
        }
    }

### @AutoBow
获取对象，并执行相关动作。

新建一个类，并实现IAutoBow接口，在shoot方法中获取对象并执行相关动作。

    // target 表示目标位置的名称
    // model  表示被注入的信息的类型
    @AutoBow(target = "routerInit", model = "router")
    public class RouterAutoBow implements IAutoBow<RouterInfoIndex> {

        @Override
        public void shoot(RouterInfoIndex routerInfoIndex) {
            Router.addRouterIndex(routerInfoIndex);
        }

    }

### @AutoTarget
在什么位置执行。

预先定义一个空方法并调用，在方法上标记@AutoTarget，例如：


    void onCreate() {
        routerInit();
    }
    
    // name 表示位置的名称
    @AutoTarget(name = "routerInit")
    void routerInit() {}

    // 可不填name，默认取方法名称。
    @AutoTarget
    void routerInit() {}

    // 可定义多个别名
    @AutoTarget(name = {"routerInit", "***Init"})
    void init() {}

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

