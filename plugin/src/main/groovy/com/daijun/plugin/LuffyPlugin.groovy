package com.daijun.plugin

import com.android.build.gradle.AppExtension
import com.daijun.plugin.bean.AutoClassFilter
import com.daijun.plugin.bean.AutoSettingsParams
import com.daijun.plugin.util.Logger;
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * @author daijun
 * @date 2018/12/27
 * @description
 */
class LuffyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('xiaoqingwa', AutoSettingsParams)
        GlobalProject.project = project
        println GlobalProject.getParams().name

        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new AutoTransform())

        project.afterEvaluate {
            Logger.setDebug(GlobalProject.getParams().isDebug)
            analysisUserConfig()
        }
    }

    def analysisUserConfig() {
        def matchData = GlobalProject.getParams().matchData
        def autoClassFilterList = new ArrayList<String>()
        matchData.each { map ->
            def classFilter = new AutoClassFilter()

            String className = map['className']
            String interfaceName = map['interfaceName']
            String methodName = map['methodName']
            String methodDesc = map['methodDesc']
            Closure methodVisitor = map['methodVisitor']
            boolean isAnnotation = map['isAnnotation']

        }
    }
}
