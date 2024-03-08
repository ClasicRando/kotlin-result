package com.github.clasicrando.kotlinresult

/**
 * Standard interface for a [ResultOf] error. Not every [ResultOf] error type argument needs to
 * extend this abstract class, but it allows a more flexible error type so different concrete error
 * types can coalesce into a single result supertype. This can be very helpful when 2 parts of your
 * application interact so your [ResultOf] type doesn't need to expand it's bound as [Any] to
 * satisfy the compiler.
 *
 * For example, if you have a top level method that depends on 2 services with the following
 * definitions:
 * ```
 * class ServiceA {
 *     fun action(): ResultOf<Int, ServiceAError> {
 *         ...
 *     }
 * }
 *
 * class ServiceB {
 *     fun action(): ResultOf<Int, ServiceBError> {
 *         ...
 *     }
 * }
 * ```
 * If you were to call these each action, and you want to return a single [ResultOf] type, you
 * would need to declare the error type as a supertype of both error types. If at least 1 doesn't
 * implement this type then you would have to go with something like this:
 * ```
 * fun topLevelMethod(serviceA: ServiceA, serviceB: ServiceB): Result<Int, Any> {
 *     val intA = when (val resultA = serviceA.action()) {
 *         is Ok -> resultA.value
 *         is Err -> return resultA.into()
 *     }
 *     val intB = when (val resultB = serviceB.action()) {
 *         is Ok -> resultB.value
 *         is Err -> result resultB.into()
 *     }
 *     return Ok(intA + intB)
 * }
 * ```
 * This doesn't help callers of this method since they have no easy way to interact with the error
 * variant since the error value is [Any]. If the error types implemented this interface, it
 * indicates to other developers that the returned value was designed as an error and provides some
 * details about the error for logging purposes.
 * ```
 * sealed class ServiceAError : StdError { .. }
 *
 * sealed class ServiceBError : StdError { .. }
 *
 * fun topLevelMethod(serviceA: ServiceA, serviceB: ServiceB): Result<Int, StdError> {
 *     val intA = when (val resultA = serviceA.action()) {
 *         is Ok -> resultA.value
 *         is Err -> {
 *             println(resultA.error.displayError())
 *             result resultA.into()
 *         }
 *     }
 *     val intB = when (val resultB = serviceB.action()) {
 *         is Ok -> resultB.value
 *         is Err -> {
 *             println(resultB.error.displayError())
 *             result resultB.into()
 *         }
 *     }
 *     return Ok(intA + intB)
 * }
 * ```
 * In most applications, you would want to construct an error type to represent all your possible
 * error variants (possibly representing other error types in wrapper variants) but this interface
 * provides a good connection between different error types.
 */
abstract class StdError {
    /**
     * Formats the error type into a message that can be displayed in the standard output or
     * included in a log message. This is separate from [toString] since we need subtypes to
     * provide error messaging and that method cannot be marked as abstract. This method is used
     * directly to provide the string output for this type.
     */
    abstract fun displayError(): String
    /**
     * Convert this type to a [Throwable]. This should only be called for logging purposes to
     * provide the error context. By default, this returns a base [Exception] where the message
     * is the result of [displayError].
     */
    open fun asThrowable(): Throwable = Exception(displayError())

    /** Returns the result of [displayError]. Cannot be overridden by subtypes. */
    final override fun toString(): String {
        return displayError()
    }
}
