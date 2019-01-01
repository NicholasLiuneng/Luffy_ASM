package com.daijun.plugin.asm

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.daijun.plugin.GlobalProject
import com.daijun.plugin.bean.AutoClassFilter
import com.daijun.plugin.util.AutoMatchUtil
import com.daijun.plugin.util.AutoTextUtil
import com.daijun.plugin.util.Logger
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

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
            // 遍历jar
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
                Logger.info("||-->结束遍历特定jar ${dest.absolutePath}")
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(modifiedJar, dest)
            }

            // 遍历目录
            transformInput.directoryInputs.each { directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
                Logger.info("||-->开始遍历特定目录 ${dest.absolutePath}")
                def dir = directoryInput.file
                if (dir) {
                    def modifyMap = new HashMap<String, File>()
                    // ~表示创建正则表达式对象，.表示匹配任意单个字符，*表示*前面的字符出现0次或者多次
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { classFile ->
                        def modifiedFile = modifyClassFile(dir, classFile, context.temporaryDir)
                        if (modifiedFile) {
                            // key为相对路径
                            modifyMap.put(
                                    classFile.absolutePath.replace(dir.absolutePath, ""),
                                    modifiedFile
                            )
                        }
                    }
                    FileUtils.copyDirectory(dir, dest)
                    modifyMap.each {key, modifiedFile ->
                        def target = new File(dest.absolutePath + key)
                        if (target.exists()) {
                            target.delete()
                        }
                        FileUtils.copyFile(modifiedFile, target)
                        modifiedFile.delete()
                    }
                }
                Logger.info("||-->结束遍历特定目录  ${dest.absolutePath}")
            }
        }
        //计算耗时
        def cost = (System.currentTimeMillis() - startTime) / 1000
        Logger.info("||=======================================================================================================")
        Logger.info("||                                       计时结束:费时${cost}秒                                           ")
        Logger.info("||=======================================================================================================")
    }

    private File modifyJarFile(File jarFile, File temporaryDir) {
        if (jarFile) {
            return modifyJar(jarFile, temporaryDir, true)
        }
        return null
    }

    private File modifyJar(File jarFile, File temporaryDir, boolean nameHex) {
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
                    modifiedClassBytes = AutoModify.modifyClasses(sourceClassBytes)
                }
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
            Logger.info("entryName = ${entryName}, className = ${className}")
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    /**
     * 目录文件中修改对应字节码
     */
    private File modifyClassFile(File dir, File classFile, File temporaryDir) {
        File modified = null
        FileOutputStream fileOutputStream = null
        try {
            def className = AutoTextUtil.path2ClassName(
                    classFile.absolutePath.replace(dir.absolutePath + File.separator, "")
            )
            Logger.info("||modifyClassFile className = ${className}")
            if (AutoMatchUtil.isShouldModifyClass(className)) {
                def sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
                def modifiedClassBytes = AutoModify.modifyClasses(sourceClassBytes)
                if (modifiedClassBytes) {
                    modified = new File(temporaryDir, "${className.replace(".", "")}.class")
                    if (modified.exists()) {
                        modified.delete()
                    }
                    modified.createNewFile()
                    fileOutputStream = new FileInputStream(modified)
                    fileOutputStream.write(modifiedClassBytes)
                }
            } else {
                return classFile
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (Exception e1) {
                    e1.printStackTrace()
                }
            }
        }
        return modified
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
