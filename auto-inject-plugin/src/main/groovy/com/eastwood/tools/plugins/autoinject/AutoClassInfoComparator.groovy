package com.eastwood.tools.plugins.autoinject

class AutoClassInfoComparator implements Comparator<AutoClassInfo> {

    @Override
    int compare(AutoClassInfo o1, AutoClassInfo o2) {
        if (o1.priority > o2.priority) {
            return 1
        } else if (o1.priority < o2.priority) {
            return -1
        } else {
            return 0
        }
    }

}