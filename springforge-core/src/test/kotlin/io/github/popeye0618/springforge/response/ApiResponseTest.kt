package io.github.popeye0618.springforge.response

import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ApiResponseTest {

    private object SampleErrorCode : ErrorCode {
        override val code: String = "SAMPLE-0001"
        override val message: String = "샘플 에러 메시지"
        override val status: Int = 400
    }

    @Test
    @DisplayName("데이터 없이 성공 응답을 생성하면 success=true, data=Unit, error=null이다")
    fun successWithoutData() {
        // given: 입력 없음

        // when
        val response = ApiResponse.success()

        // then
        assertThat(response.success).isTrue()
        assertThat(response.data).isEqualTo(Unit)
        assertThat(response.error).isNull()
    }

    @Test
    @DisplayName("데이터를 담아 성공 응답을 생성하면 동일한 데이터가 그대로 노출된다")
    fun successWithData() {
        // given
        val payload = mapOf("name" to "springforge", "version" to "0.1.0")

        // when
        val response = ApiResponse.success(payload)

        // then
        assertThat(response.success).isTrue()
        assertThat(response.data).isEqualTo(payload)
        assertThat(response.error).isNull()
    }

    @Test
    @DisplayName("ErrorCode와 fields로 실패 응답을 생성하면 code/message/fields가 ErrorResponse에 반영된다")
    fun failureWithErrorCodeAndFields() {
        // given
        val fields = listOf(
            FieldErrorResponse(field = "email", message = "형식이 올바르지 않습니다."),
            FieldErrorResponse(field = "password", message = "최소 8자 이상이어야 합니다."),
        )
        val expectedError = ErrorResponse(
            code = SampleErrorCode.code,
            message = SampleErrorCode.message,
            fields = fields,
        )

        // when
        val response = ApiResponse.failure(errorCode = SampleErrorCode, fields = fields)

        // then
        assertThat(response.success).isFalse()
        assertThat(response.error).isEqualTo(expectedError)
    }

    @Test
    @DisplayName("BusinessException으로 실패 응답을 생성하면 예외의 errorCode가 그대로 위임된다")
    fun failureWithBusinessException() {
        // given
        val exception = BusinessException(errorCode = SampleErrorCode)
        val expectedError = ErrorResponse(
            code = SampleErrorCode.code,
            message = SampleErrorCode.message,
            fields = emptyList(),
        )

        // when
        val response = ApiResponse.failure(exception)

        // then
        assertThat(response.success).isFalse()
        assertThat(response.error).isEqualTo(expectedError)
    }
}
