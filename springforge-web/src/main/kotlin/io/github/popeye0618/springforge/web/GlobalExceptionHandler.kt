package io.github.popeye0618.springforge.web

import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.CommonErrorCode
import io.github.popeye0618.springforge.error.ErrorCode
import io.github.popeye0618.springforge.response.ApiResponse
import io.github.popeye0618.springforge.response.FieldErrorResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> = toResponseEntity(e)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> =
        toResponseEntity(CommonErrorCode.INVALID_INPUT)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fields = e.bindingResult.allErrors.map { error ->
            val fieldName = if (error is FieldError) error.field else error.objectName
            FieldErrorResponse(field = fieldName, message = error.defaultMessage ?: "")
        }
        return toResponseEntity(CommonErrorCode.INVALID_INPUT, fields)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ApiResponse<Nothing>> {
        val fields = e.constraintViolations.map { violation ->
            FieldErrorResponse(
                field = violation.propertyPath.toString(),
                message = violation.message,
            )
        }
        return toResponseEntity(CommonErrorCode.INVALID_INPUT, fields)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidation(e: HandlerMethodValidationException): ResponseEntity<ApiResponse<Nothing>> {
        val fields = (e.valueResults + e.beanResults).flatMap { result ->
            result.resolvableErrors.map { error ->
                val fieldName = if (error is FieldError) error.field else result.methodParameter.parameterName ?: ""
                FieldErrorResponse(
                    field = fieldName,
                    message = error.defaultMessage ?: "",
                )
            }
        }
        return toResponseEntity(CommonErrorCode.INVALID_INPUT, fields)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> =
        toResponseEntity(CommonErrorCode.METHOD_NOT_ALLOWED)

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<Nothing>> =
        toResponseEntity(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE)

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
