package com.daijun.plugin.asm

import com.android.build.api.transform.Context
import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.daijun.plugin.GlobalProject
import com.daijun.plugin.bean.AutoClassFilter
import com.daijun.plugin.util.AutoMatchUtil
import com.daijun.plugin.util.AutoTextUtil
import com.daijun.plugin.util.Logger
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils;

import java.util.Set
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry;

/**
 * @author daijun
 * @date 2018/12/27
 * @description
 */
public class AutoTransform extends Transform {
    private static final String VERSION = "v1.0.2"

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        if (!incremental) {
            outputProvider.deleteAll()
        }
        printCopyRight()
        //开始计算消耗的时间
        Logger.info("||=======================================================================================================")
        Logger.info("||                                                 开始计时                                               ")
        Logger.info("||=======================================================================================================")
        def startTime = System.currentTimeMillis()
        if (Logger.debug) {
            printJarAndClass(inputs)
        }

        // 遍历输入文件
        inputs.each { transformInput ->
            transformInput.jarInputs.each { jarInput ->
                def destName = jarInput.file.name
                // 截取文件路径的md5值重命名输出文件，因为可能重名，会覆盖
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                // 获得输出文件
                File dest = outputProvider.getContentLocation("${destName}_${hexName}",
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                Logger.info("||-->开始遍历特定jar ${dest.absolutePath}")
                def modifiedJar = modifyJarFile(jarInput.file, context.temporaryDir)
            }
        }
    }

    private def modifyJarFile(File jarFile, File temporaryDir) {

    }

    private def mpdifyJar(File jarFile, File temporaryDir, boolean nameHex) {
        // 读取原jar
        def file = new JarFile(jarFile)
        // 设置输出的jar
        def hexName = ''
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(temporaryDir, hexName + jarFile.name)
        Logger.info("||outputJar = ${outputJar.absolutePath}")
        def jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        def entries = file.entries()
        while (entries.hasMoreElements()) {
            def jarEntry = entries.nextElement()
            def inputStream = file.getInputStream(jarEntry)
            String entryName = jarEntry.name
            String className

            def zipEntry = new ZipEntry(entryName)
            jarOutputStream.putNextEntry(zipEntry)
            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
            if (entryName.endsWith(".class")) {
                className = AutoTextUtil.path2ClassName(entryName)
                if (AutoMatchUtil.isShouldModifyClass(className)) {

                }
            }
            Logger.info("entryName = ${entryName}, className = ${className}")
        }
    }

    private def printJarAndClass(Collection<TransformInput> inputs) {
        inputs.each { transformInput ->
            transformInput.directoryInputs.each { directoryInput ->
                Logger.info("||项目class目录：${directoryInput.file.absolutePath}")
            }
            transformInput.jarInputs.each { jarInput ->
                Logger.info("||项目jar包：${jarInput.file.absolutePath}")
            }
        }
    }

    /**
     * 打印提示信息
     */
    static void printCopyRight() {
        println()
        println '#######################################################################'
        println '##########                                                    '
        println '##########         欢迎使用 Luffy® (' + VERSION + ')无埋点编译插件'
        println '##########           使用过程中碰到任何问题请联系数据中心          '
        println '##########                                                    '
        println '#######################################################################'
        println '##########                                                    '
        println '##########                      插件配置参数                    '
        println '##########                                                    '
        println '##########                 -isDebug:' + GlobalProject.getParams().isDebug
        println '##########                 -isOpenLogTrack:' + GlobalProject.getParams().isOpenLogTrack
        println '##########                 -exclude:' + GlobalProject.exclude
        println '##########                 -include:' + GlobalProject.include
        List<AutoClassFilter> autoClassFilterList = GlobalProject.autoClassFilters
        autoClassFilterList.each {
            AutoClassFilter filter ->
                println '##########                                                    '
                println '##########                 -methodName:' + filter.methodName
                println '##########                 -methodDes:' + filter.methodDesc
                println '##########                 -className:' + filter.className
                println '##########                 -interfaceName:' + filter.interfaceName
                println '##########                 -isAnnotation:' + filter.isAnnotation
        }
        println '##########                                                    '
        println '##########                                                    '
        println '#######################################################################'
        println()
    }

    @Override
    public String getName() {
        return 'AutoTrack'
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    public boolean isIncremental() {
        return false
    }
}
