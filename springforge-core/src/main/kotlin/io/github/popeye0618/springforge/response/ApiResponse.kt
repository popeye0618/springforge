package io.github.popeye0618.springforge.response

import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.ErrorCode

data class ApiResponse<out T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {

        fun success(): ApiResponse<Nothing> = ApiResponse(
            success = true,
            data = null,
        )

        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            success = true,
            data = data,
        )

        fun failure(
            errorCode: ErrorCode,
            fields: List<FieldErrorResponse> = emptyList(),
        ): ApiResponse<Nothing> = ApiResponse(
            success = false,
            error = ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                fields = fields,
            ),
        )

        fun failure(exception: BusinessException): ApiResponse<Nothing> = ApiResponse(
            success = false,
            error = ErrorResponse(
                code = exception.errorCode.code,
                message = exception.message,
                fields = emptyList()
            )
        )
    }
}
