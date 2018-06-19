package com.eastwood.tools.plugins.autoinject

import com.eastwood.tools.plugins.autoinject.adapter.BowArrowClassAdapter
import com.eastwood.tools.plugins.autoinject.adapter.OnMethodInjectListener
import com.eastwood.tools.plugins.autoinject.adapter.TargetClassAdapter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class AutoInjector {

    public static List<AutoClassInfo> autoBowInfoList = new ArrayList<>()
    public static List<AutoClassInfo> autoArrowInfoList = new ArrayList()

    public static String[] ignorePackages

    private final static BowArrowClassAdapter bowArrowClassAdapter = new BowArrowClassAdapter()

    public static void findBowAndArrow(File source) {
        if (source.isDirectory()) {
            source.eachFileRecurse { File file ->
                String filename = file.getName()
                if (filterClass(filename)) return
                ClassReader classReader = new ClassReader(file.readBytes());
                classReader.accept(bowArrowClassAdapter, 0);
            }
        } else {
            JarFile jarFile = new JarFile(source)
            Enumeration<JarEntry> entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement()
                String filename = entry.getName()
                if (filterPackage(filename)) break

                if (filterClass(filename)) continue

                InputStream stream = jarFile.getInputStream(entry)
                if (stream != null) {
                    ClassReader classReader = new ClassReader(stream.bytes);
                    classReader.accept(bowArrowClassAdapter, 0);
                    stream.close()
                }
            }
        }
    }

    private final static TargetClassAdapter targetClassAdapter = new TargetClassAdapter()

    public static void findTargetAndInject(File source) {
        if (source.isDirectory()) {
            source.eachFileRecurse { File file ->
                String filename = file.getName()
                if (filterClass(filename)) {
                    return
                }
                byte[] bytes = findTargetAndInject(source, file.readBytes())
                if (bytes != null) {
                    Logger.i('-- replace class [' + file.absolutePath + ']')
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(bytes);
                    outputStream.close();
                }
            }
        } else {
            Map<String, byte[]> tempModifiedClassByteMap = new HashMap()

            JarFile jarFile = new JarFile(source)
            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries()
            while (jarEntryEnumeration.hasMoreElements()) {
                JarEntry jarEntry = jarEntryEnumeration.nextElement()
                String filename = jarEntry.getName()
                if (filterPackage(filename)) {
                    break
                }

                if (filterClass(filename)) {
                    continue
                }

                InputStream inputStream = jarFile.getInputStream(jarEntry)
                if (inputStream != null) {
                    byte[] bytes = findTargetAndInject(source, inputStream.bytes)
                    if (bytes != null) {
                        tempModifiedClassByteMap.put(filename, bytes)
                    }
                }
                inputStream.close()
            }

            if (tempModifiedClassByteMap.size() != 0) {
                File tempJar = new File(source.absolutePath.replace('.jar', '1.jar'))
                if (tempJar.exists())
                    tempJar.delete()

                jarEntryEnumeration = jarFile.entries()
                JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJar))
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry jarEntry = jarEntryEnumeration.nextElement()
                    String filename = jarEntry.getName()
                    ZipEntry zipEntry = new ZipEntry(filename)
                    jarOutputStream.putNextEntry(zipEntry)
                    if (tempModifiedClassByteMap.containsKey(filename)) {
                        jarOutputStream.write(tempModifiedClassByteMap.get(filename))
                    } else {
                        InputStream inputStream = jarFile.getInputStream(jarEntry)
                        jarOutputStream.write(inputStream.bytes)
                        inputStream.close()
                    }
                    jarOutputStream.closeEntry()
                }
                jarOutputStream.close()

                Logger.i('-- replace jar [' + source.absolutePath + ']')
                FileOutputStream outputStream = new FileOutputStream(source);
                outputStream.write(tempJar.bytes);
                outputStream.close();
                tempJar.delete()
            }
        }
    }

    protected static byte[] findTargetAndInject(File source, byte[] bytes) {
        ClassWriter classWriter = new ClassWriter(0)
//        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
        boolean methodInject
        OnMethodInjectListener onMethodInjectListener = new OnMethodInjectListener() {
            @Override
            void onInject() {
                methodInject = true
            }
        }
        targetClassAdapter.set(classWriter, onMethodInjectListener)
        try {
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(targetClassAdapter, 0)
        } catch (Exception e) {
            String tip = "\nRead class failed when find target in source: " + source.name + "[" + source.absolutePath + "]."
            tip += "\n* Try:\n" + "     use autoInject { ignorePackages = ... } to skip this source If necessary."
            tip += "\n* Exception:\n     " + e.toString()
            for (int i = 0; i < e.stackTrace.size(); i++) {
                tip += "\n         " + e.stackTrace[i].toString()
            }
            Logger.e(tip)
        }
        if (methodInject) {
            return classWriter.toByteArray()
        } else {
            return null
        }
    }

    public static void clearBowArrow() {
        autoBowInfoList = new ArrayList<>()
        autoArrowInfoList = new ArrayList()
    }

    public static void addBowArrow(AutoClassInfo autoClassInfo) {
        Logger.i("-- find : " + autoClassInfo.toString())
        AutoType autoType = autoClassInfo.getAutoType()
        if (autoType == AutoType.BOW || autoType == AutoType.BOW_ARROW) {
            autoBowInfoList.add(autoClassInfo)
        } else if (autoType == AutoType.ARROW) {
            autoArrowInfoList.add(autoClassInfo)
        }
    }

    public static List<AutoClassInfo> getBowArrowList(String targetName) {
        List<AutoClassInfo> bowClassInfoList = new ArrayList<>()
        for (int i = 0; i < autoBowInfoList.size(); i++) {
            AutoClassInfo autoClassInfo = autoBowInfoList.get(i)
            if (autoClassInfo.target == targetName) {
                bowClassInfoList.add(autoClassInfo)
            }
        }

        AutoClassInfoComparator comparator = new AutoClassInfoComparator()
        Collections.sort(bowClassInfoList, comparator)

        List<AutoClassInfo> bowArrowInfoList = new ArrayList<>()

        for (int i = 0; i < bowClassInfoList.size(); i++) {
            AutoClassInfo bowClassInfo = bowClassInfoList.get(i)
            bowArrowInfoList.add(bowClassInfo)
            if (bowClassInfo.getAutoType() == AutoType.BOW_ARROW) continue

            List<AutoClassInfo> arrowClassInfoList = new ArrayList<>()
            for (int j = 0; j < autoArrowInfoList.size(); j++) {
                AutoClassInfo arrowClassInfo = autoArrowInfoList.get(j)
                if (arrowClassInfo.model == bowClassInfo.model) {
                    arrowClassInfoList.add(arrowClassInfo)
                }
            }
            Collections.sort(arrowClassInfoList, comparator)
            bowArrowInfoList.addAll(arrowClassInfoList)
        }
        return bowArrowInfoList
    }

    public static boolean filterPackage(String filename) {
        if (ignorePackages == null) return false

        for (int i = 0; i < ignorePackages.size(); i++) {
            if (filename.startsWith(ignorePackages[i])) {
                return true
            }
        }
        return false
    }

    public static boolean filterClass(String filename) {
        if (!filename.endsWith(".class")
                || filename.contains('R$')
                || filename.contains('R.class')
                || filename.contains("BuildConfig.class")) {
            return true
        }

        return false
    }

}