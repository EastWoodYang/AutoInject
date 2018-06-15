package com.eastwood.tools.plugins.autoinject

public class AutoClassInfo {

    public String className
    public String initDesc
    public String returnDesc

    public String target
    public String model
    public int priority

    public AutoType getAutoType() {
        if (target != null) {
            if (model != null) {
                return AutoType.BOW
            } else {
                return AutoType.BOW_ARROW
            }
        } else if (model != null) {
            return AutoType.ARROW
        } else {
            return AutoType.NONE
        }
    }

    @Override
    String toString() {
        if (target != null) {
            return 'Bow   [target: ' + target + ', model: ' + model + ', priority: ' + priority + ', className: ' + className + ']'
        } else if (model != null) {
            return 'Arrow [model: ' + model + ', priority: ' + priority + ', className: ' + className + ', returnDesc: ' + returnDesc + ']'
        } else {
            return 'None  [target: ' + target + ', model: ' + model + ', priority: ' + priority + ', className: ' + className + ', returnDesc: ' + returnDesc + ']'
        }
    }
}
