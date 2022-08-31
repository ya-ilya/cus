package me.yailya.cus.visitors

import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.informers

class CustomMethodVisitor(
    private val classLoader: ClassLoader,
    private val methodName: String,
    private val className: String,
    methodVisitor: MethodVisitor
) : MethodVisitor(Opcodes.ASM5, methodVisitor) {
    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        val superNames = CustomClassVisitor.getSuperNames(owner, classLoader)

        try {
            informers.firstOrNull {
                it.forClass == owner
                        || superNames.contains(it.forClass)
                        || it.implementations.contains(owner)
                        || superNames.any { superName -> it.implementations.contains(superName) }
            }?.also {
                it.inform(
                    opcode, owner,
                    name, desc,
                    className, methodName
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}