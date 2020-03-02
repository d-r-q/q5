package ru.jdev.q5

inline fun <reified T> logTag() =
        Tag(T::class.qualifiedName ?: T::class.simpleName ?: T::class.toString())

class Tag(private val prefix: String) {

    operator fun invoke(suffix: String? = null): String =
            "$prefix${suffix?.let { ".$it" } ?: "" }"

}
