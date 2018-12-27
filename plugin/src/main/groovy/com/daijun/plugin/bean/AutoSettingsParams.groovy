package com.daijun.plugin.bean

/**
 * 用于build.gradle中的参数传递
 */
class AutoSettingsParams {
    String name = 'Luffy字节码插件'

    boolean  isDebug = false

    /**
     * 是否打开日志采集的全埋点
     */
    boolean isOpenLogTrack = true

    /**
     * 用户自定义功能
     */
    List<Map<String, Object>> matchData = new ArrayList<>()

    /**
     * 需要手动过滤的包
     */
    List<String> exclude = []

    /**
     * 需要手动添加的包
     */
    List<String> include = []
}