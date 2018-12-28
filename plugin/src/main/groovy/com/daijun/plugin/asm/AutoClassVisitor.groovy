package com.daijun.plugin.asm;

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes;

/**
 * @author daijun
 * @date 2018/12/28
 * @description 类的遍历，遍历其中方法，满足两个条件才能修改方法字节码
 *      1、类要匹配
 *      2、方法匹配
 */
class AutoClassVisitor extends ClassVisitor {

    private ClassVisitor classVisitor
    private String className
    private String superName
    private String[] interfaces

    AutoClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM6, cv)
        classVisitor = cv
    }

    /**
     * 改方法是扫描类是第一个进入的方法，主要用于类声明使用
     * @param version 表示类版本，51，表示class文件的版本是jdk1.7
     * @param access 类的修饰符：修饰符在ASM中是以“AAC_”开头的常量的定义。
     *                  可以作用到类级别上的修饰符有：ACC_PUBLIC(public),ACC_PRIVATE(private),ACC_PROTECTED(protected),
     *                  ACC_FINAL(final),ACC_SUPER(extends),ACC_INTERFACE(interface),ACC_ABSTRACT(abstract),
     *                  ACC_SYNTHETIC(合成的),ACC_ANNOTATION(注解),ACC_ENUM(枚举),ACC_MODULE(),ACC_DEPRECATED(过时)
     * @param name 类的名称，通常类的完整类名使用“org.test.mypackage.MyClass”来表示，但是字节码中会以路径来表示，
     *              “org/test/mypackage/MyClass”，并且没有.class后缀
     * @param signature 表示泛型信息，没有定义泛型则为null
     * @param superName 表示所继承的父类，默认是Object，即java/lang/Object
     * @param interfaces 表示类实现的接口，在 Java 中类是可以实现多个不同的接口因此此处是一个数组。
     *
     * haowanyou-plugin:组件注册处理
     * https://github.com/alibaba/ARouter.git
     * haowanyou-plugin:字节码处理相关
     * https://github.com/Leaking/Hunter.git
     * haowanyou-processor:组件代码生成规则
     * haowanyou-core:组件及事件优先级处理
     * https://github.com/meituan/WMRouter.git
     * haowanyou-event:组件处理
     * https://github.com/trello/RxLifecycle.git
     * https://github.com/ReactiveX/RxJava.git
     * haowanyou-event:事件线程处理
     * https://github.com/BoltsFramework/Bolts-Android.git
     * https://github.com/square/okhttp.git
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        super.visit(version, access, name, signature, superName, interfaces)
    }
}
