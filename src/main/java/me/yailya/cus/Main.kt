package me.yailya.cus

import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import me.yailya.cus.Argument.Companion.ifExist
import me.yailya.cus.informer.Informer
import me.yailya.cus.printer.Printer
import me.yailya.cus.visitors.CustomClassVisitor
import java.io.File
import java.io.FileInputStream
import java.net.URLClassLoader
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

val printer = Printer.default()
val informers = mutableListOf(
    Informer.default("java/util/Base64"),
    Informer.default("java/net/URLClassLoader"),
    Informer.default("java/util/Base64\$Decoder")
    // You can add your class here: Informer.default("package/Class")
)

fun main(args: Array<String>) {
    val arguments = Argument.parseArguments(args)
    val zipFile = ZipFile(arguments.ifExist(
        "file",
        {
            print("Enter path to jar file (.jar): ")
            readln()
        },
        {
            it[0]
        }
    ))

    for (informer in arguments.ifExist(
        "informers",
        { emptyList() },
        { it }
    )) {
        informers.add(Informer.default(informer))
    }

    for (library in arguments.ifExist(
        "libraries",
        { emptyList() },
        { it }
    )) {
        ZipFile(library).acceptVisitor(
            URLClassLoader(
                arrayOf(File(library).toURI().toURL())
            )
        )
    }

    zipFile.acceptVisitor(
        URLClassLoader(
            arrayOf(File(zipFile.name).toURI().toURL())
        )
    )
}

fun ZipFile.acceptVisitor(classLoader: ClassLoader) {
    val acceptAfter = mutableMapOf<ClassReader, ClassVisitor>()
    val stream = ZipInputStream(FileInputStream(name))
    var entry = stream.nextEntry
    while (entry != null) {
        if (!entry.isDirectory
            && entry.name.endsWith(".class")
            && !entry.name.endsWith("module-info.class")
        ) {
            val className = entry.name.replace('/', '.').removeSuffix(".class")

            try {
                val classReader = ClassReader(getInputStream(entry).readBytes())
                val classWriter = ClassWriter(classReader, 0)
                classReader.accept(object : ClassVisitor(Opcodes.ASM5, classWriter) {
                    override fun visit(
                        version: Int,
                        access: Int,
                        name: String,
                        signature: String?,
                        superName: String,
                        interfaces: Array<out String>?
                    ) {
                        val superNames = CustomClassVisitor.getSuperNames(superName, classLoader)

                        informers.firstOrNull { informer ->
                            superNames.contains(informer.forClass) || informer.implementations.any {
                                superNames.contains(
                                    it
                                )
                            }
                        }?.implementations?.add(className.replace(".", "/"))
                    }
                }, ClassReader.EXPAND_FRAMES)

                acceptAfter[classReader] = CustomClassVisitor(classLoader, className, classWriter)
            } catch (_: Exception) {
                // Ignored
            }
        }
        entry = stream.nextEntry
    }
    stream.close()
    close()

    for ((classReader, classVisitor) in acceptAfter) {
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
    }
}

@Suppress("MemberVisibilityCanBePrivate")
data class Argument(val name: String) {
    companion object {
        fun parseArguments(args: Array<String>): List<Argument> {
            val arguments = mutableListOf<Argument>()

            for (arg in args
                .filter { it.startsWith("-") }
                .map { it.removePrefix("-") }
            ) {
                val array = arg.split("=").map { it.trim() }

                if (array.isEmpty()) {
                    continue
                }

                arguments.add(when {
                    array[1].startsWith("[") && array[1].endsWith("") -> {
                        Argument(
                            array[0],
                            array[1]
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",").map { it.trim() }
                        )
                    }

                    array.size == 1 -> Argument(array[0])
                    array.size == 2 -> Argument(array[0], array[1])
                    else -> continue
                })
            }

            return arguments
        }

        fun <T> List<Argument>.ifExist(
            name: String,
            `else`: () -> T,
            action: (List<String>) -> T
        ): T {
            return firstOrNull { it.name == name }?.let { action(it.values) } ?: `else`()
        }
    }

    val values = mutableListOf<String>()
    var type = Type.Default

    constructor(name: String, value: String) : this(name) {
        this.values.add(value)
        this.type = Type.Value
    }

    constructor(name: String, values: List<String>) : this(name) {
        this.values.addAll(values)
        this.type = Type.Array
    }

    enum class Type {
        Default, // -argument
        Value, // -argument=value
        Array // -argument=[value1, value2]
    }
}