package com.daijun.plugin.util;

/**
 * @author daijun
 * @date 2018/12/27
 * @description
 */
class AutoTextUtil {

    static boolean isEmpty(String text) {
        text == null || text.trim().length() < 1
    }

    static String path2ClassName(String pathName) {
        pathName.replace(File.separator, '.').replace('.class', '')
    }

    static String changeClassName2Path(String className) {
        className.replace('.', File.separator)
    }
}
