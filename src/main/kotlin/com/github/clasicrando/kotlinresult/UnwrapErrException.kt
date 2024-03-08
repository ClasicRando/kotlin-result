package com.github.clasicrando.kotlinresult

class UnwrapErrException(value: Any?)
    : Exception("Attempted to unwrap a ResultOf but the value was Ok = $value")