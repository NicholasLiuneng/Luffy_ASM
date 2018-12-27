package com.daijun.plugin

import com.android.build.gradle.AppExtension
import com.daijun.plugin.bean.AutoClassFilter
import com.daijun.plugin.bean.AutoSettingsParams
import com.daijun.plugin.util.AutoTextUtil
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
        def autoClassFilterList = new ArrayList<AutoClassFilter>()
        matchData.each { map ->
            def classFilter = new AutoClassFilter()

            String className = map['className']
            String interfaceName = map['interfaceName']
            String methodName = map['methodName']
            String methodDesc = map['methodDesc']
            Closure methodVisitor = map['methodVisitor']
            boolean isAnnotation = map['isAnnotation']

            // 类的全类名
            if (!AutoTextUtil.isEmpty(className)) {
                className = AutoTextUtil.changeClassName2Path(className)
            }

            // 接口的全类名
            if (!AutoTextUtil.is(interfaceName)) {
                interfaceName = AutoTextUtil.changeClassName2Path(interfaceName)
            }

            classFilter.className = className
            classFilter.interfaceName = interfaceName
            classFilter.methodName = methodName
            classFilter.methodDesc = methodDesc
            classFilter.methodVisitor = methodVisitor
            classFilter.isAnnotation = isAnnotation
            autoClassFilterList.add(classFilter)
        }

        GlobalProject.autoClassFilters = autoClassFilterList

        // 需要手动添加的包
        def includePackages = GlobalProject.getParams().include
        def include = new HashSet<String>()
        if (includePackages != null) {
            include.addAll(includePackages)
        }
        GlobalProject.include = include

        def excludePackages = GlobalProject.getParams().exclude
        def exclude = new HashSet<String>()
        exclude.add('android.support')
        exclude.add('androidx')
        if (excludePackages != null) {
            exclude.addAll(excludePackages)
        }
        GlobalProject.exclude = exclude
    }
}
