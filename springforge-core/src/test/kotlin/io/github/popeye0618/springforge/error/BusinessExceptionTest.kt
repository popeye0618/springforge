package io.github.popeye0618.springforge.error

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BusinessExceptionTest {

    private object SampleErrorCode : ErrorCode {
        override val code: String = "SAMPLE-0001"
        override val message: String = "샘플 에러 메시지"
        override val status: Int = 400
    }

    @Test
    @DisplayName("errorCode만 전달하면 message는 errorCode.message가 기본값이고 cause는 null이다")
    fun defaultMessageAndCause() {
        // given
        val errorCode = SampleErrorCode

        // when
        val exception = BusinessException(errorCode = errorCode)

        // then
        assertThat(exception.errorCode).isSameAs(errorCode)
        assertThat(exception.message).isEqualTo(errorCode.message)
        assertThat(exception.cause).isNull()
    }

    @Test
    @DisplayName("message를 명시적으로 지정하면 errorCode.message 대신 해당 값이 사용된다")
    fun overrideMessage() {
        // given
        val customMessage = "커스텀 메시지"

        // when
        val exception = BusinessException(
            errorCode = SampleErrorCode,
            message = customMessage,
        )

        // then
        assertThat(exception.message).isEqualTo(customMessage)
    }

    @Test
    @DisplayName("cause를 전달하면 RuntimeException cause로 그대로 전파된다")
    fun propagateCause() {
        // given
        val rootCause = IllegalStateException("원인 예외")

        // when
        val exception = BusinessException(
            errorCode = SampleErrorCode,
            cause = rootCause,
        )

        // then
        assertThat(exception.cause).isSameAs(rootCause)
    }
}
