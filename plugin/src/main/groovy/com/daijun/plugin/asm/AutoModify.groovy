package com.daijun.plugin.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter;

/**
 * @author daijun
 * @date 2018/12/28
 * @description 修改字节码
 */
public class AutoModify {

    static byte[] modifyClasses(byte[] srcClassBytes) {
        byte[] classByteCode = null
        try {
            classByteCode = modifyClass(srcClassBytes)
        } catch (Exception e) {
            e.printStackTrace()
        }
        if (classByteCode == null) {
            classByteCode = srcClassBytes
        }
        return classByteCode
    }

    private static byte[] modifyClass(byte[] srcClassBytes) throws Exception{
        def classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        def classVisitor = new AutoClassVisitor(classWriter)
        def classReader = new ClassReader(srcClassBytes)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

}
