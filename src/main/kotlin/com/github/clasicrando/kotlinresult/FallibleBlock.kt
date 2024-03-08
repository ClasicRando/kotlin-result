package com.github.clasicrando.kotlinresult

class FallibleBlock<T, E> {
    @PublishedApi
    internal var error: E? = null

    fun tryUnwrap(result: ResultOf<T, E>): T {
        return when (result) {
            is Err -> {
                error = result.error
                throw BindException()
            }
            is Ok -> result.value
        }
    }

    fun tryRun(block: () -> ResultOf<T, E>): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return when (val result = block()) {
            is Err -> {
                error = result.error
                throw BindException()
            }
            is Ok -> result.value
        }
    }
}

inline fun <T, E> tryBlock(block: FallibleBlock<T, E>.() -> T): ResultOf<T, E> {
    val fallibleBlock = FallibleBlock<T, E>()
    return try {
        Ok(fallibleBlock.block())
    } catch (ex: BindException) {
        val error = fallibleBlock.error ?: error("Bind Exception thrown but no error collected")
        Err(error)
    }
}