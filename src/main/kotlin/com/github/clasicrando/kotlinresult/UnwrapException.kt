package com.github.clasicrando.kotlinresult

class UnwrapException(error: Any?)
    : Exception("Attempted to unwrap a ResultOf but the value was Err = $error")
