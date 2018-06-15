package com.eastwood.tools.plugins.autoinject.adapter

import com.eastwood.tools.plugins.autoinject.AutoClassInfo
import com.eastwood.tools.plugins.autoinject.AutoInjector
import com.eastwood.tools.plugins.autoinject.AutoType
import org.gradle.api.GradleScriptException
import org.objectweb.asm.*

class BowArrowClassAdapter extends ClassVisitor {

    private final static String AUTO_BOW_ANNOTATION_BYTECODE = "Lcom/eastwood/common/autoinject/AutoBow;"
    private final static String AUTO_ARROW_ANNOTATION_BYTECODE = "Lcom/eastwood/common/autoinject/AutoArrow;"
    private final static String AUTO_BOW_ARROW_ANNOTATION_BYTECODE = "Lcom/eastwood/common/autoinject/AutoBowArrow;"
    private final static String AUTO_BOW_INTERFACE_BYTECODE = "com/eastwood/common/autoinject/IAutoBow"
    private final static String AUTO_ARROW_INTERFACE_BYTECODE = "com/eastwood/common/autoinject/IAutoArrow"
    private final static String AUTO_BOW_ARROW_INTERFACE_BYTECODE = "com/eastwood/common/autoinject/IAutoBowArrow"
    private final static String AUTO_ARROW_SIGNATURE_PREFIX_BYTECODE = "Lcom/eastwood/common/autoinject/IAutoArrow<"

    private String classname
    private String signature
    private String[] interfaces
    private AutoClassInfo autoClassInfo

    private boolean context
    private List<String> getMethodDescList

    BowArrowClassAdapter() {
        super(Opcodes.ASM4)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.classname = name
        this.signature = signature
        this.interfaces = interfaces
        autoClassInfo = null
        context = false
        getMethodDescList = null
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (AUTO_BOW_ANNOTATION_BYTECODE == desc || AUTO_ARROW_ANNOTATION_BYTECODE == desc || AUTO_BOW_ARROW_ANNOTATION_BYTECODE == desc) {
            AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible)
            OnAnnotationValueListener valueListener = new OnAnnotationValueListener() {
                @Override
                void onValue(String name, Object value) {
                    if (autoClassInfo == null) {
                        autoClassInfo = new AutoClassInfo()
                        getMethodDescList = new ArrayList<>()
                    }

                    switch (name) {
                        case "target":
                            autoClassInfo.target = value
                            break
                        case "model":
                            autoClassInfo.model = value
                            break
                        case "priority":
                            autoClassInfo.priority = value
                            break
                        case "context":
                            context = value
                            break
                    }

                }
            }
            return new AnnotationAdapter(annotationVisitor, valueListener)
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (autoClassInfo != null) {
            if (name == '<init>') {
                if (autoClassInfo.initDesc != null) {
                    throw new GradleScriptException("class " + classname + " has multi-<init>.", null);
                } else if (context && desc == '()V') {
                    throw new GradleScriptException("class " + classname + " need context, but has a non-parameter constructor.", null)
                } else if (!context && desc != '()V') {
                    throw new GradleScriptException("class " + classname + " is not need context, but without a non-parameter constructor.", null)
                }
                autoClassInfo.initDesc = desc
            } else if (name == 'get' && autoClassInfo.getAutoType() == AutoType.ARROW) {
                if (Type.getArgumentTypes(desc).length == 0) {
                    getMethodDescList.add(desc)
                }
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        if (autoClassInfo != null) {
            AutoType autoType = autoClassInfo.getAutoType()
            if (autoType == AutoType.ARROW) {
                if (!interfaces.contains(AUTO_ARROW_INTERFACE_BYTECODE)) {
                    throw new GradleScriptException("class " + classname + " should implements " + AUTO_ARROW_INTERFACE_BYTECODE + " directly.", null);
                }

                if (this.signature == null) {
                    throw new GradleScriptException("class " + classname + " implements " + AUTO_ARROW_INTERFACE_BYTECODE + " must specified generic type.", null)
                }

                String returnDesc = getReturnDesc()
                if (returnDesc == null) {
                    throw new GradleScriptException("can't find returnDesc of class " + classname + ".get(...).", null);
                }
                autoClassInfo.returnDesc = returnDesc
            } else if (autoType == AutoType.BOW) {
                if (!interfaces.contains(AUTO_BOW_INTERFACE_BYTECODE)) {
                    throw new GradleScriptException("class " + classname + " should implements " + AUTO_BOW_INTERFACE_BYTECODE + " directly.", null);
                }
            } else if (autoType == AutoType.BOW_ARROW) {
                if (!interfaces.contains(AUTO_BOW_ARROW_INTERFACE_BYTECODE)) {
                    throw new GradleScriptException("class " + classname + " should implements " + AUTO_BOW_ARROW_INTERFACE_BYTECODE + " directly.", null);
                }
            }
            autoClassInfo.className = classname
            AutoInjector.addBowArrow(autoClassInfo)
        }
    }


    String getReturnDesc() {
        String returnDesc = null
        for (int i = 0; i < getMethodDescList.size(); i++) {
            String desc = getMethodDescList.get(i)
            desc = desc.subSequence(2, desc.length() - 1)
            if (this.signature.contains(AUTO_ARROW_SIGNATURE_PREFIX_BYTECODE + desc)) {
                returnDesc = desc + ';'
                break
            }
        }
        return returnDesc
    }
}