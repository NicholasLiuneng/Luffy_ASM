package com.daijun.plugin.asm

import com.daijun.plugin.util.LogAnalyticsUtil
import com.daijun.plugin.util.Logger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/12/28
 * @description 针对日志采集sdk埋点的方法进行修改
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
    private boolean isHasTracked

    protected LogMethodVisitor(MethodVisitor mv, int access, String name, String desc,
                               String className, String superName, String[] interfaces,
                               HashSet<String> visitedFragmentMethods) {
        super(Opcodes.ASM6, mv, access, name, desc)
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
        if (desc == 'Lcom/mmc/lamandys/liba_datapick/annotation/AutoTrackDataViewOnClick;') {
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
            def logMethidCell = LogHookConfig.sLambdaMethods.get(desc)
            if (logMethidCell != null) {
                int paramsStart = logMethidCell.paramsStart
                if (LogAnalyticsUtil.isStatic(mAccess)) {
                    paramsStart -= 1
                }
                LogAnalyticsUtil.visitMethodWithLoadedParams(mv, INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                        logMethidCell.agentName, logMethidCell.agentDesc, paramsStart, logMethidCell.paramsCount, logMethidCell.opcodes)
                isHasTracked = true
                return
            }
        }

        // 如果不是公共的普通方法，就不埋点
        if (!(LogAnalyticsUtil.isPublic(mAccess) && !LogAnalyticsUtil.isStatic(mAccess))) {
            return
        }

        // 之前添加过埋点代码，忽略
        if (isHasInstrumented) {
            return
        }

        // 方法描述信息
        def methodNameDesc = methodName + methodDesc

        // fragment,目前支持 android/support/v4/app/ListFragment 和 android/support/v4/app/Fragment
        if (LogAnalyticsUtil.isInstanceOfFragment(mSuperName)) {
            def logMethodCell = LogHookConfig.sFragmentMethods.get(methodNameDesc)
            if (logMethodCell != null) {
                mVisitedFragmentMethods.add(methodNameDesc)
                LogAnalyticsUtil.visitMethodWithLoadedParams(mv, INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                        logMethodCell.agentName, logMethodCell.agentDesc, logMethodCell.paramsStart,
                        logMethodCell.paramsCount, logMethodCell.opcodes)
                isHasTracked = true
            }
        }

        // menu, 目前支持 onContextItemSelected(MenuItem item)、onOptionsItemSelected(MenuItem item)
        if (LogAnalyticsUtil.isTargetMenuMethodDesc(methodNameDesc)) {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE, 'trackMenuItem',
                    "(Ljava/lang/Object;Landroid/view/MenuItem;)V", false)
            isHasTracked = true
            return
        }

        if (methodNameDesc == 'onDrawerOpened(Landroid/view/View;)V') {
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                    "trackDrawerOpened", "(Landroid/view/View;)V", false)
            isHasTracked = true
            return
        }

        if (methodNameDesc == 'onDrawerClosed(Landroid/view/View;)V') {
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                    "trackDrawerClosed", "(Landroid/view/View;)V", false)
            isHasTracked = true
            return
        }

        if (mClassName == 'android/databinding/generated/callback/OnClickListener') {
            if (methodNameDesc == 'onClick(Landroid/view/View;)V') {
                mv.visitVarInsn(ALOAD, 1)
                mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                        "trackViewOnClick", "(Landroid/view/View;)V", false)
                isHasTracked = true
                return
            }
        }

        if (mClassName.startsWith("android") || mClassName.startsWith("androidx")) {
            return
        }

        if (methodNameDesc == 'onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V'
                || methodNameDesc == 'onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V') {
            mv.visitVarInsn(ALOAD, 1)
            mv.visitVarInsn(ALOAD, 2)
            mv.visitVarInsn(ALOAD, 3)
            mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE, "trackListView",
                    "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
            isHasTracked = true
            return
        }

        if (isAutoTrackViewOnClickAnnotation) {
            if (methodNameDesc == '(Landroid/view/View;)V') {
                mv.visitVarInsn(ALOAD, 1)
                mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                        "trackViewOnClick", "(Landroid/view/View;)V", false)
                isHasTracked = true
                return
            }
        }

        if (mInterfaces != null && mInterfaces.length > 0) {
            def logMethodCell = LogHookConfig.sInterfaceMethods.get(methodNameDesc)
            if (logMethodCell != null && mInterfaces.contains(logMethodCell.parent)) {
                LogAnalyticsUtil.visitMethodWithLoadedParams(mv, INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                        logMethodCell.agentName, logMethodCell.agentDesc, logMethodCell.paramsStart,
                        logMethodCell.paramsCount, logMethodCell.opcodes)
                isHasTracked = true
                return
            }
        }

        if (!isHasTracked) {
            if (methodNameDesc == 'onClick(Landroid/view/View;)V') {
                mv.visitVarInsn(ALOAD, 1)
                mv.visitMethodInsn(INVOKESTATIC, LogHookConfig.LOG_ANALYTICS_BASE,
                        "trackViewOnClick", "(Landroid/view/View;)V", false)
                isHasTracked = true
            }
        }
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        if (isHasTracked) {
            visitAnnotation('Lcom/mmc/lamandys/liba_datapick/AutoDataInstrumented;', false)
            Logger.info("||Hooked method：${methodName}${methodDesc}")
        }
        Logger.info("||结束扫描方法：${methodName}")
    }
}
