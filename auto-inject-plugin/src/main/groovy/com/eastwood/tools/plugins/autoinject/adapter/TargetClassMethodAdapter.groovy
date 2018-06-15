package com.eastwood.tools.plugins.autoinject.adapter

import com.eastwood.tools.plugins.autoinject.AutoClassInfo
import com.eastwood.tools.plugins.autoinject.AutoInjector
import com.eastwood.tools.plugins.autoinject.AutoType
import com.eastwood.tools.plugins.autoinject.Logger
import org.gradle.api.GradleScriptException
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class TargetClassMethodAdapter extends MethodVisitor {

    private final static String AUTO_TARGET_ANNOTATION_BYTECODE = "Lcom/eastwood/common/autoinject/AutoTarget;"

    String className
    String methodName
    String methodDesc

    private boolean needInject
    private OnMethodInjectListener onInjectListener
    private List<String> targetNames
    private int currentLocals = 1
    private boolean contextStack

    TargetClassMethodAdapter() {
        super(Opcodes.ASM4)
    }

    void set(MethodVisitor methodVisitor, String className, String methodName, String desc) {
        this.mv = methodVisitor
        this.className = className
        this.methodName = methodName
        targetNames = new ArrayList<>()
        methodDesc = desc
    }

    void setMethodInjectListener(OnMethodInjectListener listener) {
        this.onInjectListener = listener
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (AUTO_TARGET_ANNOTATION_BYTECODE == desc) {
            needInject = true
            AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible)
            OnAnnotationValueListener valueListener = new OnAnnotationValueListener() {
                @Override
                void onValue(String name, Object value) {
                    if (name == null && value != null) {
                        if (!targetNames.contains(value)) {
                            targetNames.add(value)
                        }
                    } else if (name != null && name == "name") {
                        if (!targetNames.contains(value)) {
                            targetNames.add(value)
                        }
                    }
                }
            }
            return new AnnotationAdapter(annotationVisitor, valueListener)
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    void visitCode() {
        if (!needInject) {
            mv.visitCode()
            return
        }

        if (targetNames.isEmpty()) {
            targetNames.add(methodName)
        }

        List<AutoClassInfo> autoClassInfoList = new ArrayList<>()
        for (int i = 0; i < targetNames.size(); i++) {
            autoClassInfoList.addAll(AutoInjector.getBowArrowList(targetNames[i]))
        }
        if (autoClassInfoList.size() == 0) {
            needInject = false
            mv.visitCode()
            return
        }
        Logger.i("--\n-- inject class: " + className + ", method: " + methodName + ", targets: " + targetNames.toString())
        onInjectListener.onInject()

        String currentBowClassName
        int currentBowPosition
        int offset = getTypesSize(Type.getArgumentTypes(methodDesc))
        for (int i = 0; i < autoClassInfoList.size(); i++) {
            AutoClassInfo autoClassInfo = autoClassInfoList.get(i)
            AutoType autoType = autoClassInfo.getAutoType()
            if (autoType == AutoType.BOW || autoType == AutoType.BOW_ARROW) {
                currentLocals += 1
                currentBowPosition = offset + i + 1
                currentBowClassName = autoClassInfo.className
                mv.visitTypeInsn(Opcodes.NEW, autoClassInfo.className)
                mv.visitInsn(Opcodes.DUP)
                if (autoClassInfo.initDesc != "()V") {
                    contextStack = true
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                }
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, autoClassInfo.className, '<init>', autoClassInfo.initDesc, false)
                mv.visitVarInsn(Opcodes.ASTORE, offset + i + 1)

                if (autoType == AutoType.BOW_ARROW) {
                    mv.visitVarInsn(Opcodes.ALOAD, currentBowPosition)
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, currentBowClassName, 'shoot', '()V', false)
                    Logger.i("-- bow_arrow  , " + "target: " + autoClassInfo.target + ', className: ' + autoClassInfo.className)
                } else {
                    Logger.i("-- bow        , " + "target: " + autoClassInfo.target + ", model: " + autoClassInfo.model + ', className: ' + autoClassInfo.className)
                }
            } else if (autoType == AutoType.ARROW) {
                currentLocals += 1
                mv.visitTypeInsn(Opcodes.NEW, autoClassInfo.className)
                mv.visitInsn(Opcodes.DUP)
                if (autoClassInfo.initDesc != "()V") {
                    contextStack = true
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                }
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, autoClassInfo.className, '<init>', autoClassInfo.initDesc, false)
                mv.visitVarInsn(Opcodes.ASTORE, offset + i + 1)

                mv.visitVarInsn(Opcodes.ALOAD, currentBowPosition)
                mv.visitVarInsn(Opcodes.ALOAD, offset + i + 1)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, autoClassInfo.className, 'get', '()' + autoClassInfo.returnDesc, false)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, currentBowClassName, 'shoot', '(' + autoClassInfo.returnDesc + ')V', false)

                Logger.i("-- arrow      , model: " + autoClassInfo.model + ', className: ' + autoClassInfo.className + ', returnDesc: ' + autoClassInfo.returnDesc)
            } else {
                throw new GradleScriptException("Unknown AutoType: " + autoClassInfo.toString(), null);
            }
        }
    }

    @Override
    void visitMaxs(int maxStack, int maxLocals) {
        if (needInject) {
            if (maxStack < 2) {
                if (contextStack) {
                    maxStack = 3
                } else {
                    maxStack = 2
                }
            }

            if (maxLocals < currentLocals) {
                maxLocals = currentLocals
            }
        }
        super.visitMaxs(maxStack, maxLocals)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        needInject = false
        targetNames = new ArrayList<>()
        currentLocals = 1
        contextStack = false

    }

    private static int getTypesSize(Type[] types) {
        int size = 0
        for (int i = 0; i < types.size(); i++) {
            size += types[i].getSize()
        }
        return size
    }

}