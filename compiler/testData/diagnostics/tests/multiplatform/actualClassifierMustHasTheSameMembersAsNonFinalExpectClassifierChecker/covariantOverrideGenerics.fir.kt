// MODULE: m1-common
// FILE: common.kt

open class Base<R> {
    open fun foo(): R = null!!
}

<!EXPECT_ACTUAL_INCOMPATIBILITY{JVM}, EXPECT_ACTUAL_INCOMPATIBILITY{JVM}!>expect open class Foo<R, T : R> : Base<R> {
}<!>

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual open class <!NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS!>Foo<!><E, F : E> : Base<E>() {
    override fun foo(): F = null!!
}
