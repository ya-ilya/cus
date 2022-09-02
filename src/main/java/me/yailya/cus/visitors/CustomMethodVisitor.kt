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
        try {
            informers.firstOrNull { it.isMatch(owner, classLoader) }?.also {
                it.onMethodCall(
                    opcode, owner,
                    name, desc,
                    className, methodName
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
        try {
            informers.firstOrNull { it.isMatch(owner, classLoader) }?.also {
                it.onFieldCall(
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