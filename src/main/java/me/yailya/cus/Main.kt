package me.yailya.cus

import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import me.yailya.cus.informer.Informer
import me.yailya.cus.informer.informers.B64DecoderInformer
import me.yailya.cus.printer.Printer
import me.yailya.cus.visitors.CustomClassVisitor
import java.io.File
import java.io.FileInputStream
import java.net.URLClassLoader
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

val printer = Printer.default()
val informers = listOf(
    Informer.default("java/util/Base64"),
    Informer.default("java/net/URLClassLoader"),
    B64DecoderInformer()
    // You can add your class here: Informer.default("package/Class")
)

fun main(args: Array<String>) {
    val zipFile = ZipFile(run {
        if (args.any { it.startsWith("-file=") }) {
            args.first { it.startsWith("-file=") }.removePrefix("-file=")
        } else {
            print("Enter path to jar file (.jar): ")
            readln()
        }
    })
    val classLoader = URLClassLoader(
        arrayOf(File(zipFile.name).toURI().toURL()),
        Any::class.java.classLoader
    )
    val classes = mutableMapOf<String, ByteArray>()

    ZipInputStream(FileInputStream(zipFile.name)).forEach {
        if (!it.isDirectory && it.name.endsWith(".class")) {
            classes[it.name.replace('/', '.').removeSuffix(".class")] =
                zipFile.getInputStream(it).readBytes()
        }
    }

    for ((className, bytes) in classes) {
        try {
            val classReader = ClassReader(bytes)
            val classWriter = ClassWriter(classReader, 0)
            val classVisitor = CustomClassVisitor(classLoader, className, classWriter)
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

fun ZipInputStream.forEach(action: (ZipEntry) -> Unit) {
    var entry = nextEntry
    while (entry != null) {
        action(entry)
        entry = nextEntry
    }
    close()
}