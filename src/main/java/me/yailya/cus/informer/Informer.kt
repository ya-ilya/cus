package me.yailya.cus.informer

import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.printer

/**
 * @param forClass Class name
 */
// TODO: Make informers
@Suppress("unused")
abstract class Informer(val forClass: String) {
    companion object {
        fun default(forClass: String) = object : Informer(forClass) {}
    }

    val implementations = mutableListOf<String>()

    /**
     * @param opcode Opcode that was used when calling the method. See [Opcodes]
     * @param owner Method owner
     * @param name Method name
     * @param descriptor Method descriptor
     * @param callerClass Class, where the method was called from
     * @param callerMethod Method in [callerClass] where the method was called from
     * @param arguments Call arguments
     */
    open fun inform(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        callerClass: String,
        callerMethod: String
    ) {
        printer.print(
            "Method Owner: $owner",
            "Method Name: $name",
            "Method Descriptor: $descriptor",
            "Called from $callerMethod in $callerClass"
        )
    }

    /**
     * @return Name of [opcode]
     */
    protected fun getOpcodeName(opcode: Int): String {
        return Opcodes::class.java.fields.first { it.get(null) == opcode }.name
    }
}