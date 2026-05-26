package io.github.popeye0618.springforge.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.CommonErrorCode
import io.github.popeye0618.springforge.error.ErrorCode
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.test.Test

class GlobalExceptionHandlerTest {

    private lateinit var mockMvc: MockMvc

    data class SampleRequest(@field:NotBlank val name: String)

    @RestController
    class TestController {

        @GetMapping("/business-exception")
        fun throwBusinessException(): Nothing = throw BusinessException(
            errorCode = object : ErrorCode {
                override val code = "TEST-4000"
                override val message = "테스트 비즈니스 예외"
                override val status = 400
            }
        )

        @PostMapping("/validation")
        fun validation(@RequestBody @Valid request: SampleRequest): String = request.name

        @GetMapping("/exception")
        fun throwException(): Nothing = throw RuntimeException("일반 예외")

        @GetMapping("/access-denied")
        fun throwAccessDenied(): Nothing = throw org.springframework.security.access.AccessDeniedException("권한 없음")

        @GetMapping("/response-status")
        fun throwResponseStatus(): Nothing = throw ResponseStatusException(HttpStatus.FORBIDDEN, "접근 금지")

        @GetMapping("/response-status-unmapped")
        fun throwResponseStatusUnmapped(): Nothing = throw ResponseStatusException(HttpStatus.CONFLICT, "충돌 발생")

        @GetMapping("/constraint-violation")
        fun throwConstraintViolation(): Nothing =
            throw ConstraintViolationException(emptySet())

        @GetMapping("/global-error")
        fun throwGlobalError(): Nothing {
            val bindingResult = BeanPropertyBindingResult(Any(), "request")
            bindingResult.reject("mismatch", "비밀번호가 일치하지 않습니다.")
            val param = MethodParameter(TestController::class.java.getDeclaredMethod("throwGlobalError"), -1)
            throw MethodArgumentNotValidException(param, bindingResult)
        }
    }

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(LocalValidatorFactoryBean().apply { afterPropertiesSet() })
            .setMessageConverters(
                MappingJackson2HttpMessageConverter(
                    ObjectMapper().registerKotlinModule()
                )
            )
            .build()
    }

    @Test
    @DisplayName("BusinessException이 발생하면 errorCode.status와 code/message가 응답에 반영된다")
    fun handleBusinessException() {
        // given: /business-exception 엔드포인트가 code=TEST-4000, status=400인 BusinessException을 던짐

        // when
        val result = mockMvc.perform(get("/business-exception")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("TEST-4000")
        assertThat(result.response.contentAsString).contains("테스트 비즈니스 예외")
    }

    @Test
    @DisplayName("@Valid 검증 실패 시 400과 INVALID_INPUT code, 실패한 필드 정보가 응답된다")
    fun handleMethodArgumentNotValid() {
        // given: name 필드가 빈 문자열인 요청 (NotBlank 위반)

        // when
        val result = mockMvc.perform(
            post("/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": ""}""")
        ).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.INVALID_INPUT.code)
        assertThat(result.response.contentAsString).contains("name")
    }

    @Test
    @DisplayName("클래스 레벨 검증 실패로 globalError가 발생하면 objectName이 field에 담겨 응답된다")
    fun handleGlobalValidationError() {
        // given: 클래스 레벨 validator가 globalError를 등록한 MethodArgumentNotValidException을 던짐

        // when
        val result = mockMvc.perform(get("/global-error")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.INVALID_INPUT.code)
        assertThat(result.response.contentAsString).contains("request")
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드로 요청하면 405와 METHOD_NOT_ALLOWED가 응답된다")
    fun handleMethodNotSupported() {
        // given: GET만 지원하는 엔드포인트에 POST 요청

        // when
        val result = mockMvc.perform(post("/business-exception")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(405)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.METHOD_NOT_ALLOWED.code)
    }

    @Test
    @DisplayName("ResponseStatusException이 발생하면 예외의 status에 맞는 ErrorCode로 응답된다")
    fun handleResponseStatusException() {
        // given: /response-status 엔드포인트가 403 ResponseStatusException을 던짐

        // when
        val result = mockMvc.perform(get("/response-status")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(403)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.FORBIDDEN.code)
    }

    @Test
    @DisplayName("매핑되지 않은 상태 코드의 ResponseStatusException이 발생하면 원래 상태 코드와 동적 ErrorCode가 응답된다")
    fun handleResponseStatusExceptionUnmapped() {
        // given: /response-status-unmapped 엔드포인트가 409 ResponseStatusException을 던짐

        // when
        val result = mockMvc.perform(get("/response-status-unmapped")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(409)
        assertThat(result.response.contentAsString).contains("COMMON-409")
        assertThat(result.response.contentAsString).contains("충돌 발생")
    }

    @Test
    @DisplayName("잘못된 형식의 JSON 요청 시 400과 INVALID_INPUT이 응답된다")
    fun handleHttpMessageNotReadable() {
        // given: 유효하지 않은 JSON을 request body로 전송

        // when
        val result = mockMvc.perform(
            post("/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid-json")
        ).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.INVALID_INPUT.code)
    }

    @Test
    @DisplayName("ConstraintViolationException이 발생하면 400과 INVALID_INPUT이 응답된다")
    fun handleConstraintViolation() {
        // given: /constraint-violation 엔드포인트가 ConstraintViolationException을 던짐

        // when
        val result = mockMvc.perform(get("/constraint-violation")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.INVALID_INPUT.code)
    }

    @Test
    @DisplayName("AccessDeniedException은 500으로 처리하지 않고 rethrow하여 Spring Security가 처리하도록 한다")
    fun handleAccessDeniedException() {
        // given: /access-denied 엔드포인트가 AccessDeniedException을 던짐

        // when: rethrow된 예외는 ServletException으로 감싸져 전파됨
        val exception = assertThrows<jakarta.servlet.ServletException> {
            mockMvc.perform(get("/access-denied")).andReturn()
        }

        // then: cause가 AccessDeniedException — 500 응답이 아닌 Spring Security에게 위임
        assertThat(exception.cause)
            .isInstanceOf(org.springframework.security.access.AccessDeniedException::class.java)
    }

    @Test
    @DisplayName("처리되지 않은 Exception이 발생하면 500과 INTERNAL_SERVER_ERROR가 응답된다")
    fun handleException() {
        // given: /exception 엔드포인트가 RuntimeException을 던짐

        // when
        val result = mockMvc.perform(get("/exception")).andReturn()

        // then
        assertThat(result.response.status).isEqualTo(500)
        assertThat(result.response.contentAsString).contains(CommonErrorCode.INTERNAL_SERVER_ERROR.code)
    }
}
