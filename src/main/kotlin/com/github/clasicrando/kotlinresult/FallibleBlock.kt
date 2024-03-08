package com.github.clasicrando.kotlinresult

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class FallibleBlockException : Exception(
    "Exception to indicate a binding has encountered a ResultOf.Err. " +
    "This should never bubble up to you so if that is the case, please submit an issue on github."
)

class FallibleBlock<T, E> {
    @PublishedApi
    internal var error: E? = null

    fun tryUnwrap(result: ResultOf<T, E>): T {
        return when (result) {
            is Err -> {
                error = result.error
                throw FallibleBlockException()
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
                throw FallibleBlockException()
            }
            is Ok -> result.value
        }
    }
}

inline fun <T, E> tryBlock(block: FallibleBlock<T, E>.() -> T): ResultOf<T, E> {
    val fallibleBlock = FallibleBlock<T, E>()
    return try {
        Ok(fallibleBlock.block())
    } catch (ex: FallibleBlockException) {
        val error = fallibleBlock.error ?: error("Bind Exception thrown but no error collected")
        Err(error)
    }
}