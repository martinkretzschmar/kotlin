// TARGET_BACKEND: JVM
// IGNORE_BACKEND_K2: JVM_IR, JS_IR, JS_IR_ES6
// FIR status: scripts aren't supported yet
// IGNORE_LIGHT_ANALYSIS
// WITH_STDLIB
// FILE: test.kt

fun box(): String =
    Script.Build.Debug.run { "${c0()}${c1()}" }

// FILE: script.kts

interface Base {
    val v: String

    fun c0(): Char {
        fun getC0() = v[0]
        return getC0()
    }
}

enum class Build(override val v: String): Base {
    Debug("OK"),
    Release("NO");

    fun c1(): Char {
        val g = object {
            val c1 = v[1]
        }
        return g.c1
    }
}