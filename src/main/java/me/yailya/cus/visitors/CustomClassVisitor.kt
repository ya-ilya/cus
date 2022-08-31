package me.yailya.cus.visitors

import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class CustomClassVisitor(
    private val classLoader: ClassLoader,
    private val className: String,
    classVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM5, classVisitor) {

    companion object {
        fun getSuperNames(superName: String, classLoader: ClassLoader): List<String> {
            val superNames = mutableListOf(superName.replace("/", "."))

            try {
                var currentSuperName = superNames[0]

                while (currentSuperName != "java.lang.Object") {
                    currentSuperName = Class.forName(currentSuperName, false, classLoader).superclass.name
                    superNames.add(currentSuperName)
                }
            } catch (_: Throwable) {
                // Ignored
            }

            return superNames.map { it.replace(".", "/") }
        }
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        return CustomMethodVisitor(
            classLoader,
            name, className,
            super.visitMethod(access, name, desc, signature, exceptions)
        )
    }
}