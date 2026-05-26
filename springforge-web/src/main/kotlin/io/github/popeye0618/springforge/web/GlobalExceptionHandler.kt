package io.github.popeye0618.springforge.web

import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.CommonErrorCode
import io.github.popeye0618.springforge.error.ErrorCode
import io.github.popeye0618.springforge.response.ApiResponse
import io.github.popeye0618.springforge.response.FieldErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException) = toResponseEntity(e)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fields = e.bindingResult.allErrors.map { error ->
            val fieldName = if (error is FieldError) error.field else error.objectName
            FieldErrorResponse(field = fieldName, message = error.defaultMessage ?: "")
        }
        return toResponseEntity(CommonErrorCode.INVALID_INPUT, fields)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException) =
        toResponseEntity(CommonErrorCode.METHOD_NOT_ALLOWED)

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException) =
        toResponseEntity(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(e: NoResourceFoundException) =
        toResponseEntity(CommonErrorCode.RESOURCE_NOT_FOUND)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(e: ResponseStatusException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = when (e.statusCode.value()) {
            400 -> CommonErrorCode.INVALID_INPUT
            401 -> CommonErrorCode.UNAUTHORIZED
            403 -> CommonErrorCode.FORBIDDEN
            404 -> CommonErrorCode.RESOURCE_NOT_FOUND
            405 -> CommonErrorCode.METHOD_NOT_ALLOWED
            415 -> CommonErrorCode.UNSUPPORTED_MEDIA_TYPE
            else -> CommonErrorCode.INTERNAL_SERVER_ERROR
        }
        return toResponseEntity(errorCode)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception", e)
        return toResponseEntity(CommonErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun toResponseEntity(
        errorCode: ErrorCode,
        fields: List<FieldErrorResponse> = emptyList(),
    ): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(errorCode.status)
            .body(ApiResponse.failure(errorCode, fields))

    private fun toResponseEntity(exception: BusinessException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(exception.errorCode.status)
            .body(ApiResponse.failure(exception))
}
