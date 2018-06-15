package com.eastwood.tools.plugins.autoinject

import org.gradle.api.Project

public class Logger {

    public static Project project
    public static boolean showLog

    public static void i(String message) {
        if (showLog) project.getLogger().println(message)
    }

    public static void e(String message) {
        project.getLogger().error(message)
    }

}
