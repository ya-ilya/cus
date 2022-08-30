package me.yailya.cus.visitors

import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.informers

@Suppress("SpellCheckingInspection")
class CustomMethodVisitor(
    private val methodName: String,
    private val className: String,
    methodVisitor: MethodVisitor
) : MethodVisitor(Opcodes.ASM5, methodVisitor) {
    private var ldcs = mutableListOf<Any>()

    companion object {
        private val descriptorRegex = ".*\\((.*?)\\).*".toRegex()
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        informers.firstOrNull { it.forClass == owner }?.also {
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