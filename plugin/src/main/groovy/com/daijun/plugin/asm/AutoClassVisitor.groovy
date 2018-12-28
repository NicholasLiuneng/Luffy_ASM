package com.daijun.plugin.asm

import com.daijun.plugin.GlobalProject
import com.daijun.plugin.util.Logger;
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
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
    private String mClassName
    private String mSuperName
    private String[] mInterfaces

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
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mClassName = name
        mSuperName = superName
        mInterfaces = interfaces
        Logger.info("\n||开始扫描类：${mClassName}")
        Logger.info("||类详情：version = ${version}, access = ${Logger.accCode2String(access)}, signature = ${signature}," +
                "superName = ${superName},interfaces = ${interfaces.toArrayString()}")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    /**
     * 扫描类的方法
     * @param access 方法的修饰符
     * @param name 方法名，在asm中visitMethod方法会处理构造方法，静态代码块，私有方法，受保护的方法，共有方法，native类型方法
     *                      构造方法的方法名是<init>，静态代码块的方法名是<clinit>
     * @param desc 方法签名，格式：（参数列表类型）返回值类型
     * @param signature 方法的泛型信息，并且该值基本等于方法签名，只不过泛型参数被特殊标记
     * @param exceptions 表示方法是否抛出异常，没有则null
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        def methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        MethodVisitor adapter = null
        if (GlobalProject.isOpenLogTrack()) {

        }
        return super.visitMethod(access, name, desc, signature, exceptions)
    }
}
