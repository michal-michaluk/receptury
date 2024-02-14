package documentation.generator;

import lombok.Builder;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Builder
record PerspectiveParameters(
        Predicate<Span> scenarioPredicate,
        Predicate<Span> include,
        Predicate<Span> exclude,
        Function<Span, String> participantName,
        Function<Span, String> participantGroup,
        Function<Span, String> callName,
        Function<Span, Stream<Map.Entry<String, Object>>> argumentsFilter) {

    String participantName(Span span) {
        return participantName.apply(span);
    }

    Stream<Map.Entry<String, Object>> argumentsFilter(Span span) {
        return argumentsFilter.apply(span);
    }
}
