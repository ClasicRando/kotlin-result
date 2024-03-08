package com.github.clasicrando.kotlinresult

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class ResultOf<T, E> {
    fun unwrap(): T {
        return when (this) {
            is Err -> throw UnwrapException(this.error)
            is Ok -> this.value
        }
    }

    fun unwrapErr(): E {
        return when (this) {
            is Err -> this.error
            is Ok -> throw UnwrapErrException(this.value)
        }
    }

    fun unwrapOr(default: T): T {
        return when (this) {
            is Err -> default
            is Ok -> this.value
        }
    }

    fun unwrapOrElse(default: () -> T): T {
        contract {
            callsInPlace(default, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Err -> default()
            is Ok -> this.value
        }
    }

    inline fun <R> map(block: (T) -> R): ResultOf<R, E> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Err -> this.into()
            is Ok -> Ok(block(this.value))
        }
    }

    inline fun <R> mapErr(block: (E) -> R): ResultOf<T, R> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Err -> Err(block(this.error))
            is Ok -> this.into()
        }
    }

    inline fun <R> andThen(block: (T) -> ResultOf<R, E>): ResultOf<R, E> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Err -> this.into()
            is Ok -> block(this.value)
        }
    }
}

data class Ok<T, E>(val value: T) : ResultOf<T, E>() {
    @Suppress("UNCHECKED_CAST")
    fun <F> into(): ResultOf<T, F> {
        return this as ResultOf<T, F>
    }
}

data class Err<T, E>(val error: E) : ResultOf<T, E>() {
    @Suppress("UNCHECKED_CAST")
    fun <R> into(): ResultOf<R, E> {
        return this as ResultOf<R, E>
    }
}

fun <T> Result<T>.toResultOf(): ResultOf<T, Throwable> {
    return when {
        this.isFailure -> Err(this.exceptionOrNull()!!)
        else -> Ok(this.getOrNull()!!)
    }
}

fun <T, E> Iterable<ResultOf<T, E>>.collectOrErr(): ResultOf<List<T>, E> {
    return Ok(map {
        when (it) {
            is Err -> return Err(it.error)
            is Ok -> it.value
        }
    })
}
