package me.yailya.cus.visitors

import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.informers

class CustomClassVisitor(
    private val classLoader: ClassLoader,
    private val className: String,
    classVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM5, classVisitor) {
    val visitActions = mutableListOf<() -> Unit>()
    val methodVisitors = mutableListOf<CustomMethodVisitor>()

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

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>?
    ) {
        visitActions.add {
            val superNames = getSuperNames(superName, classLoader)

            informers.firstOrNull { informer ->
                superNames.contains(informer.forClass) || informer.implementations.any { superNames.contains(it) }
            }?.implementations?.add(className.replace(".", "/"))
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
        ).also { methodVisitors.add(it) }
    }
}