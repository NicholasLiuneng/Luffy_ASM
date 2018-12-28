package com.daijun.plugin.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter;

/**
 * @author daijun
 * @date 2018/12/28
 * @description 修改字节码
 */
public class AutoModify {

    static void modifyClasses(byte[] srcClassBytes) {
        byte[] classByteCode = null

    }

    static void modifyClass(byte[] srcClassBytes) {
        def classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)

    }

}
