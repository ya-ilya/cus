package me.yailya.cus.informer

import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.printer
import me.yailya.cus.visitors.CustomClassVisitor

/**
 * @param forClass Class name
 */
@Suppress("unused")
abstract class Informer(val forClass: String) {
    companion object {
        fun default(forClass: String) = object : Informer(forClass) { }
    }

    val implementations = mutableListOf<String>()

    /**
     * @param opcode Opcode that was used when calling the method. See [Opcodes]
     * @param owner Method owner
     * @param name Method name
     * @param descriptor Method descriptor
     * @param callerClass Class, where the method was called from
     * @param callerMethod Method in [callerClass] where the method was called from
     */
    open fun informMethod(
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
     * @param opcode Opcode that was used when calling the field. See [Opcodes]
     * @param owner Field owner
     * @param name Field name
     * @param descriptor Field descriptor
     * @param callerClass Class, where the field was called from
     * @param callerMethod Method in [callerClass] where the field was called from
     */
    open fun informField(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        callerClass: String,
        callerMethod: String
    ) {
        printer.print(
            "Field Owner: $owner",
            "Field Name: $name",
            "Field Descriptor: $descriptor",
            "Called from $callerMethod in $callerClass"
        )
    }

    fun isMatch(clazz: String, classLoader: ClassLoader): Boolean {
        val superNames = CustomClassVisitor.getSuperNames(clazz, classLoader)

        return forClass == clazz
                || implementations.contains(clazz)
                || superNames.contains(forClass)
                || superNames.any { superName -> implementations.contains(superName) }
    }

    /**
     * @return Name of [opcode]
     */
    protected fun getOpcodeName(opcode: Int): String {
        return Opcodes::class.java.fields.first { it.get(null) == opcode }.name
    }
}