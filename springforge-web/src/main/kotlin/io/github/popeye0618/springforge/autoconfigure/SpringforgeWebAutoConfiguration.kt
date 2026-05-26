package io.github.popeye0618.springforge.autoconfigure

import io.github.popeye0618.springforge.web.GlobalExceptionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)
class SpringforgeWebAutoConfiguration {

    @Bean
    fun globalExceptionHandler() = GlobalExceptionHandler()
}