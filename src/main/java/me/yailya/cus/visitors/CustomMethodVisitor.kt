package me.yailya.cus.visitors

import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.informers

@Suppress("SpellCheckingInspection")
class CustomMethodVisitor(
    private val classLoader: ClassLoader,
    private val methodName: String,
    private val className: String,
    methodVisitor: MethodVisitor
) : MethodVisitor(Opcodes.ASM5, methodVisitor) {
    private var ldcs = mutableListOf<Any>()

    companion object {
        private val descriptorRegex = ".*\\((.*?)\\).*".toRegex()
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        val superNames = mutableListOf(owner.replace("/", "."))

        try {
            var superName = superNames[0]

            while (superName != "java.lang.Object") {
                superName = Class.forName(superName, false, classLoader).superclass.name
                superNames.add(superName.replace(".", "/"))
            }
        } catch (ex: Exception) {
            // Ignored
        }

        informers.firstOrNull {
            it.forClass == owner || superNames.contains(it.forClass)
        }?.also {
            it.inform(
                opcode, owner,
                name, desc,
                className, methodName,
                try {
                    ldcs.takeLast(
                        descriptorRegex
                            .find(desc)!!.groupValues[1]
                            .split(";").size
                    )
                } catch (ex: Exception) {
                    emptyList()
                }
            )
        }

        ldcs.clear()
    }

    override fun visitLdcInsn(cst: Any) {
        ldcs.add(cst)
    }
}