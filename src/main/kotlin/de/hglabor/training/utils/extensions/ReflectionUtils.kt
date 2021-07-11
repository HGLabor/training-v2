package de.hglabor.training.utils.extensions

/**
 * Get's the return value of the reflected function for this object.
 */
fun <T> Any.reflectMethod(method: String): T {
    val reflectedMethod = this::class.java.getMethod(method)
    @Suppress("UNCHECKED_CAST")
    return reflectedMethod.invoke(this) as T
}
