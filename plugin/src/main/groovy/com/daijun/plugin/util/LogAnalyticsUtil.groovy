package com.daijun.plugin.util

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/12/28
 * @description
 */
public class LogAnalyticsUtil implements Opcodes {
    private static final def targetFragmentClass = new HashSet<String>()
    private static final def targetMenuMethodDesc = new HashSet<String>()

    static {
        // Menu
        targetMenuMethodDesc.add("onContextItemSelected(Landroid/view/MenuItem;)Z")
        targetMenuMethodDesc.add("onOptionsItemSelected(Landroid/view/MenuItem;)Z")
        targetMenuMethodDesc.add("onNavigationItemSelected(Landroid/view/MenuItem;)Z")

        // Fragment
        targetFragmentClass.add('android/support/v4/app/Fragment')
        targetFragmentClass.add('android/support/v4/app/ListFragment')
        targetFragmentClass.add('android/support/v4/app/DialogFragment')

        // For AndroidX Fragment
        targetFragmentClass.add('androidx/fragment/app/Fragment')
        targetFragmentClass.add('androidx/fragment/app/ListFragment')
        targetFragmentClass.add('androidx/fragment/app/DialogFragment')
    }

    static boolean isSynthetic(int access) {
        return (access & ACC_SYNTHETIC) != 0
    }

    static boolean isPrivate(int access) {
        return (access & ACC_PRIVATE) != 0
    }

    static boolean isPublic(int access) {
        return (access & ACC_PUBLIC) != 0
    }

    static boolean isStatic(int access) {
        return (access & ACC_STATIC) != 0
    }

    static boolean isTargetMenuMethodDesc(String desc) {
        return targetMenuMethodDesc.contains(desc)
    }

    static boolean isTargetFragmentClass(String className) {
        return targetFragmentClass.contains(className)
    }

    static boolean isInstanceOfFragment(String superName) {
        return targetFragmentClass.contains(superName)
    }

    static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner,
                                            String methodName, String methodDesc, int start, int count,
                                            List<Integer> opcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(opcodes[i - start], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
    }
}
