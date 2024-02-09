package arch.doc;

import io.opentelemetry.javaagent.shaded.io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.assertj.TracesAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.function.Consumer;

public class OpenTelemetryFacade {
    private final OpenTelemetrySdk openTelemetry;
    private final InMemorySpanExporter spanExporter;
    private final InMemoryMetricReader metricReader;
    private final InMemoryLogRecordExporter logRecordExporter;

    private OpenTelemetryFacade(OpenTelemetrySdk openTelemetry, InMemorySpanExporter spanExporter, InMemoryMetricReader metricReader, InMemoryLogRecordExporter logRecordExporter) {
        this.openTelemetry = openTelemetry;
        this.spanExporter = spanExporter;
        this.metricReader = metricReader;
        this.logRecordExporter = logRecordExporter;
    }

    public static OpenTelemetryFacade create() {
//        InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
//        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();
//        InMemoryMetricReader metricReader = InMemoryMetricReader.create();
//        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
//        InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();
//        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder().addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter)).build();
//        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
//                .setPropagators(ContextPropagators.noop())
//                .setTracerProvider(tracerProvider)
//                .setMeterProvider(meterProvider)
//                .setLoggerProvider(loggerProvider)
//                .build();
//        return new OpenTelemetryFacade(openTelemetry, spanExporter, metricReader, logRecordExporter);
        return null;
    }

    public TracesAssert assertTraces() {
        return TracesAssert.assertThat(this.spanExporter.getFinishedSpanItems());
    }

    public void printTraces(Consumer<SpanData> consumer) {
        this.spanExporter.getFinishedSpanItems().forEach(consumer);
    }

    private void clearSpans() {
        this.spanExporter.reset();
    }

    private void clearMetrics() {
        SdkMeterProviderUtil.resetForTest(this.openTelemetry.getSdkMeterProvider());
    }

    private void clearLogRecords() {
        this.logRecordExporter.reset();
    }

    public void open() {
//        GlobalOpenTelemetry.resetForTest();
//        GlobalOpenTelemetry.set(this.openTelemetry);
    }

    public void beforeEachScenario() {
        this.clearSpans();
        this.clearMetrics();
        this.clearLogRecords();
    }

    public void close() {
        GlobalOpenTelemetry.resetForTest();
        this.openTelemetry.close();
    }
}
