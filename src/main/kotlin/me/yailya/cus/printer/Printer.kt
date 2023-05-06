package me.yailya.cus.printer

abstract class Printer {
    companion object {
        fun default() = object : Printer() {}
    }

    open fun print(vararg lines: String?) {
        val linesNotNull = lines.filterNotNull()
        val sCount = linesNotNull.maxOf { it.length }

        println("╔═${"═".repeat(sCount)}═╗")

        for (line in linesNotNull) {
            println("║ $line ${" ".repeat(sCount - line.length)}║")
        }

        println("╚═${"═".repeat(sCount)}═╝")
    }
}