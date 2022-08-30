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
        ldcs: List<Any>
    ) {
        printer.print(
            "Method Owner: $owner",
            "Method Name: $name",
            "Method Descriptor: $descriptor",
            "Method Argument: ${ldcs.firstOrNull()}".takeIf { ldcs.isNotEmpty() },
            "Decoded Argument: ${Base64.getDecoder().decode(ldcs.firstOrNull().toString()).toString(Charsets.UTF_8)}"
                .takeIf { ldcs.isNotEmpty() },
            "Called from $callerMethod in $callerClass"
        )
    }
}