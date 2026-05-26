package io.github.popeye0618.springforge.autoconfigure

import io.github.popeye0618.springforge.web.GlobalExceptionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RestControllerAdvice

@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)
class SpringforgeWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(annotation = [RestControllerAdvice::class])
    @ConditionalOnProperty(
        prefix = "springforge.web.exception-handler",
        name = ["enabled"],
        matchIfMissing = true,
    )
    fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()
}
