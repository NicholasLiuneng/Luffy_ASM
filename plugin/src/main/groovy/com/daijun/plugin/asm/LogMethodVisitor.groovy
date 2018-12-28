package com.daijun.plugin.asm

import com.daijun.plugin.util.LogAnalyticsUtil
import com.daijun.plugin.util.Logger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/12/28
 * @description
 */
public class LogMethodVisitor extends AdviceAdapter {

    private int mAccess
    private String methodName
    private String methodDesc
    private String mClassName
    private String mSuperName
    private String[] mInterfaces
    private HashSet<String> mVisitedFragmentMethods
    private boolean isAutoTrackViewOnClickAnnotation
    private boolean isAutoTrackIgnoreTrackOnClick
    private boolean isHasInstrumented

    protected LogMethodVisitor(MethodVisitor mv, int access, String name, String desc,
                               String className, String superName, String[] interfaces,
                               HashSet<String> visitedFragmentMethods) {
        super(ASM6, mv, access, name, desc)
        mAccess = access
        methodName = name
        methodDesc = desc
        mClassName = className
        mSuperName = superName
        mInterfaces = interfaces
        mVisitedFragmentMethods = visitedFragmentMethods
        Logger.info("||开始扫描方法：${Logger.accCode2String(access)} ${name}${desc}")
    }

    /**
     * 扫描到方法的注解
     * @param desc 注解的签名，它使用的是（L类型路径;）形式表述
     * @param visible 运行时注解是否可见，asm可以探测到RetentionPolicy.CLASS和RetentionPolicy.RUNTIME声明的注解
     * @return
     */
    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        Logger.info("||扫描到方法的注解：${desc}")
        if (desc == 'Lcom/mmc/lamandys/liba_datapick/AutoTrackDataViewOnClick;') {
            isAutoTrackViewOnClickAnnotation = true
            Logger.info("||发现 ${methodName}${methodDesc}发现有注解 @AutoTrackDataViewOnClick")
        }
        if (desc == 'Lcom/mmc/lamandys/liba_datapick/AutoIgnoreTrackDataOnClick;') {
            isAutoTrackIgnoreTrackOnClick = true
            Logger.info("||发现 ${methodName}${methodDesc}发现有注解 @AutoIgnoreTrackDataOnClick")
        }
        if (desc == 'Lcom/mmc/lamandys/liba_datapick/AutoDataInstrumented;') {
            isHasInstrumented = true
            Logger.info("||发现 ${methodName}${methodDesc}发现有注解 @AutoDataInstrumented")
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        if (isAutoTrackIgnoreTrackOnClick) {
            return
        }

        // 在gradle 3.2.1版本中针对view的setOnClickListener方法的lambda表达式做特殊处理
        if (methodName.trim().startsWith('lambda$') && LogAnalyticsUtil.isPrivate(mAccess)
                && LogAnalyticsUtil.isSynthetic(mAccess)) {

        }
    }
}
