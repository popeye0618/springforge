package io.github.popeye0618.springforge.error

class BusinessException (
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
    cause: Throwable? = null
) : RuntimeException(message, cause)