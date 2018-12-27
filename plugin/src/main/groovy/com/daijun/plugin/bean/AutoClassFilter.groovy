package com.daijun.plugin.bean;

/**
 * @author daijun
 * @date 2018/12/27
 * @description
 */
class AutoClassFilter {
    String className = ''
    String interfaceName = ''
    String methodName = ''
    String methodDesc = ''
    Closure methodVisitor
    boolean isAnnotation = false
}
