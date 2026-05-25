package io.github.popeye0618.springforge.response

data class ErrorResponse(
    val code: String,
    val message: String,
    val fields: List<FieldErrorResponse> = emptyList()
)
