package documentation.generator;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.TracesData;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.platform.commons.function.Try;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.READ;

class Telemetry {
    @NotNull
    static TelemetrySpans fromProtoFiles(Stream<Path> traces) {
        Stream<TracesData> tracesDataStream = traces
                .map(path -> Try.call(() -> TracesData.parseFrom(Files.newInputStream(path, READ)))
                        .getOrThrow(RuntimeException::new));

        return fromProtoTracesData(tracesDataStream);
    }

    @NotNull
    static TelemetrySpans fromProtoTracesData(Stream<TracesData> tracesDataStream) {
        var graph = DefaultDirectedGraph.createBuilder(DefaultEdge.class);
        Map<ByteString, Span> spans = tracesDataStream
                .flatMap(trace -> trace.getResourceSpansList().stream())
                .flatMap(trace -> trace.getScopeSpansList().stream())
                .flatMap(trace -> trace.getSpansList().stream())
                .map(span -> Span.builder()
                        .name(span.getName())
                        .spanId(span.getSpanId())
                        .parentSpanId(span.getParentSpanId())
                        .start(Instant.ofEpochSecond(span.getStartTimeUnixNano() / 1_000_000_000L, span.getStartTimeUnixNano() % 1_000_000_000L))
                        .end(Instant.ofEpochSecond(span.getEndTimeUnixNano() / 1_000_000_000L, span.getEndTimeUnixNano() % 1_000_000_000L))
                        .attributes(span.getAttributesList().stream().collect(Collectors.toUnmodifiableMap(
                                KeyValue::getKey, attrib -> attrib.getValue().getStringValue()
                        )))
                        .build())
                .peek(span -> graph.addEdge(span.parentSpanId(), span.spanId()))
                .collect(Collectors.toMap(
                        Span::spanId,
                        span -> span
                ));
        return new TelemetrySpans(spans, graph.buildAsUnmodifiable());
    }
}
