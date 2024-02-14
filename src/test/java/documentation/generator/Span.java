package documentation.generator;

import com.google.protobuf.ByteString;
import lombok.Builder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
record Span(
        String name,
        ByteString spanId,
        ByteString parentSpanId,
        Instant start,
        Instant end,
        Map<String, Object> attributes
) {

    String toString(Set<String> attributes) {
        return "span:" +
               "\n name: " + name +
               "\n spanId: " + Base64.getEncoder().encodeToString(spanId.toByteArray()) +
               "\n parentSpanId: " + Base64.getEncoder().encodeToString(parentSpanId.toByteArray()) +
               "\n start: " + start +
               "\n end: " + end +
               this.attributes.entrySet().stream()
                       .filter(entry -> attributes.contains(entry.getKey()))
                       .map(entry -> "\n " + entry.getKey() + ": " + entry.getValue())
                       .collect(Collectors.joining("")) +
               "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Span span = (Span) o;
        return Objects.equals(spanId, span.spanId);
    }

    Optional<Map.Entry<String, Object>> anyOfAttributes(Set<String> filter) {
        return attributes.entrySet().stream()
                .filter(entry -> filter.contains(entry.getKey()))
                .findFirst();
    }

    Stream<Map.Entry<String, Object>> attributes(Set<String> filter) {
        return attributes.entrySet().stream()
                .filter(entry -> filter.contains(entry.getKey()));
    }

    Optional<String> attribute(String attribute) {
        return Optional.ofNullable((String) attributes.get(attribute));
    }

    @Override
    public int hashCode() {
        return Objects.hash(spanId);
    }
}
