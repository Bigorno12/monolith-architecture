package mu.server.rest.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
@EnableScheduling
class SchedulingConfig {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SchedulingConfig::class.java)
    }

    @Bean
    fun taskScheduler(): TaskScheduler {
        return ThreadPoolTaskScheduler().apply {
            poolSize = 4
            setThreadNamePrefix("scheduler-")
            isRemoveOnCancelPolicy = true
            setAwaitTerminationSeconds(300)
            setWaitForTasksToCompleteOnShutdown(true)
            setErrorHandler { t -> LOGGER.error("Uncaught in scheduled pool tasks: ${t.message}") }
            initialize()
        }
    }
}