package com.daijun.plugin.util

import com.daijun.plugin.GlobalProject;

/**
 * @author daijun
 * @date 2018/12/28
 * @description
 */
public class AutoMatchUtil {

    /**
     * 是否对扫描到的类进行修改
     * @param className
     * @return
     */
    static boolean isShouldModifyClass(String className) {
        if (className.contains('R$') || className.contains('R2$') || className.endsWith('R')
                || className.endsWith('R2') || className.endsWith('BuildConfig')) {
            return false
        }
        // 用户自定义的优先通过
        GlobalProject.include.each { packageName ->
            if (className.startsWith(packageName)) {
                return true
            }
        }

        // 不允许通过的包，包括用户自定义的
        GlobalProject.exclude.each { packageName ->
            if (className.startsWith(packageName)) {
                return false
            }
        }
        return true
    }
}
