package com.eastwood.tools.plugins.autoinject.adapter

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

class AnnotationAdapter extends AnnotationVisitor {

    OnAnnotationValueListener valueListener

    AnnotationAdapter(AnnotationVisitor annotationVisitor, OnAnnotationValueListener listener) {
        super(Opcodes.ASM4, annotationVisitor)
        this.valueListener = listener
    }

    @Override
    void visit(String name, Object value) {
        super.visit(name, value)
        valueListener.onValue(name, value)
    }

    @Override
    AnnotationVisitor visitArray(String name) {
        if (name == "name") {
            return new AnnotationAdapter(super.visitArray(name), valueListener)
        } else {
            return super.visitArray(name)
        }
    }

}