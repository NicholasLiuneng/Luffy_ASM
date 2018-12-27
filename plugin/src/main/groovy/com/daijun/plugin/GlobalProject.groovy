package com.daijun.plugin

import com.daijun.plugin.bean.AutoClassFilter
import com.daijun.plugin.bean.AutoSettingsParams
import org.gradle.api.Project;

/**
 * @author daijun
 * @date 2018/12/27
 * @description
 */
class GlobalProject {

    static Project project

    static List<AutoClassFilter> autoClassFilters

    static HashSet<String> exclude

    static HashSet<String> include

    static AutoSettingsParams getParams() {
        return project.xiaoqingwa
    }

    static isOpenLogTrack() {
        return getParams().isOpenLogTrack
    }


}
