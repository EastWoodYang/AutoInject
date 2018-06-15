package com.eastwood.tools.plugins.autoinject.adapter

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class TargetClassAdapter extends ClassVisitor {

    TargetClassMethodAdapter targetClassMethodAdapter = new TargetClassMethodAdapter()

    String className

    TargetClassAdapter() {
        super(Opcodes.ASM4)
    }

    void set(ClassVisitor classWriter, OnMethodInjectListener onMethodInjectListener) {
        this.cv = classWriter
        targetClassMethodAdapter.setMethodInjectListener(onMethodInjectListener)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
    }


    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        targetClassMethodAdapter.set(methodVisitor, className, name, desc)
        return targetClassMethodAdapter
    }

}