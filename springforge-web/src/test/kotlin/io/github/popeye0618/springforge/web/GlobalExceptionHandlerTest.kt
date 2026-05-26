package io.github.popeye0618.springforge.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.popeye0618.springforge.error.BusinessException
import io.github.popeye0618.springforge.error.CommonErrorCode
import io.github.popeye0618.springforge.error.ErrorCode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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