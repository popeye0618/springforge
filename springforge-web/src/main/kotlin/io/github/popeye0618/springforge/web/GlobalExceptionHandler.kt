package io.github.popeye0618.springforge.web

import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.CommonErrorCode
import io.github.popeye0618.springforge.response.ApiResponse
import io.github.popeye0618.springforge.response.FieldErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.failure(e)
        return ResponseEntity.status(e.errorCode.status).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fields = e.bindingResult.allErrors.mapNotNull { error ->
            if (error is FieldError) {
                FieldErrorResponse(field = error.field, message = error.defaultMessage ?: "")
            } else null
        }

        val response = ApiResponse.failure(errorCode = CommonErrorCode.INVALID_INPUT, fields = fields)
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT.status).body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.failure(errorCode = CommonErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity.status(CommonErrorCode.METHOD_NOT_ALLOWED.status).body(response)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.failure(errorCode = CommonErrorCode.UNSUPPORTED_MEDIA_TYPE)
        return ResponseEntity.status(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.status).body(response)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(e: NoResourceFoundException): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.failure(errorCode = CommonErrorCode.RESOURCE_NOT_FOUND)
        return ResponseEntity.status(CommonErrorCode.RESOURCE_NOT_FOUND.status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val response = ApiResponse.failure(errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.status).body(response)
    }
}