package documentation.generator;

import com.google.protobuf.ByteString;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

record TelemetrySpans(Map<ByteString, Span> spans,
                      Graph<Object, DefaultEdge> graph) {
    Span span(Object spanId) {
        return spans.get(spanId);
    }

    Stream<Span> select(Predicate<Span> predicate) {
        return spans.values().stream().filter(predicate);
    }
}
