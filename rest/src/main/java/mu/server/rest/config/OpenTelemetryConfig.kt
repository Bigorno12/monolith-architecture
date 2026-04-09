package mu.server.rest.config

import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmMemoryMeterConventions
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmThreadMeterConventions
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention


@Configuration(proxyBeanMethods = false)
class OpenTelemetryConfig {

    @Bean
    fun openTelemetryServerRequestObservationConvention(): OpenTelemetryServerRequestObservationConvention =
        OpenTelemetryServerRequestObservationConvention()

    @Bean
    fun openTelemetryJvmCpuMeterConventions(): OpenTelemetryJvmCpuMeterConventions =
        OpenTelemetryJvmCpuMeterConventions(Tags.empty())

    @Bean
    fun processorMetrics(): ProcessorMetrics =
        ProcessorMetrics(listOf(), OpenTelemetryJvmCpuMeterConventions(Tags.empty()))

    @Bean
    fun jvmMemoryMetrics(): JvmMemoryMetrics =
        JvmMemoryMetrics(listOf(), OpenTelemetryJvmMemoryMeterConventions(Tags.empty()))


    @Bean
    fun jvmThreadMetrics(): JvmThreadMetrics =
        JvmThreadMetrics(listOf(), OpenTelemetryJvmThreadMeterConventions(Tags.empty()))


    @Bean
    fun classLoaderMetrics(): ClassLoaderMetrics =
        ClassLoaderMetrics(OpenTelemetryJvmClassLoadingMeterConventions())
}