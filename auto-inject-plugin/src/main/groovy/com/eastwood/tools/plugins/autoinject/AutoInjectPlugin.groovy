package com.eastwood.tools.plugins.autoinject

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoInjectPlugin implements Plugin<Project> {

    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (!isApp) return

        project.extensions.create("autoInject", AutoInjectExtension.class);

        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new AutoInjectTransform(project))

        project.afterEvaluate {
            Logger.project = project
            Logger.showLog = project.autoInject.showLog
            AutoInjector.filterPackages = project.autoInject.filterPackages
        }

    }

}