package com.daijun.plugin.util

import com.daijun.plugin.GlobalProject
import com.daijun.plugin.bean.AutoClassFilter;

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

    /**
     * 是否修改用户自定义方法
     * 匹配规则有三种：
     * 1、有注解的话全部匹配
     * 2、类名+方法名+方法签名匹配
     * 3、接口名+方法名+方法签名匹配
     * @param filter
     * @param className
     * @param methodName
     * @param methodDesc
     * @param interfaces
     * @return
     */
    static boolean isShouldModifyCustomMethod(AutoClassFilter filter, String className, String methodName,
                                              String methodDesc, String[] interfaces) {
        // 1、自定义方法如果需要注解的话就得每个方法进行遍历操作
        if (filter.isAnnotation) {
            return
        }
        boolean isMatchClass = filter.className == className
        boolean isMatchMethod = filter.methodName == methodName
        boolean isMatchMethodDesc = filter.methodDesc == methodDesc
        boolean isMatchInterface
        interfaces.each { mInterface ->
            if (filter.interfaceName == mInterface) {
                isMatchInterface = true
            }
        }

        // 方法名和方法签名匹配，类或者接口要匹配
        return (isMatchClass || isMatchInterface) && isMatchMethod && isMatchMethodDesc
    }
}
