package me.yailya.cus.informer.informers

import me.yailya.cus.informer.Informer
import me.yailya.cus.printer
import java.util.*

class B64DecoderInformer : Informer("java/util/Base64\$Decoder") {
    override fun inform(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        callerClass: String,
        callerMethod: String,
        arguments: List<Any>
    ) {
        printer.print(
            "Method Owner: $owner",
            "Method Name: $name",
            "Method Descriptor: $descriptor",
            "Method Argument: ${arguments[0]}",
            "Decoded Argument: ${Base64.getDecoder().decode(arguments[0].toString()).toString(Charsets.UTF_8)}",
            "Called from $callerMethod in $callerClass"
        )
    }
}