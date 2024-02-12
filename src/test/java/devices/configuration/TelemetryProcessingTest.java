package devices.configuration;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.TracesData;
import lombok.Builder;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.READ;

public class TelemetryProcessingTest {

    @Test
    void processProto() throws IOException {
        process(Files.list(Path.of("traces")));
    }

    public static void process(Stream<Path> traces) {
        var builder = DefaultDirectedGraph.createBuilder(DefaultEdge.class);

        Map<ByteString, Span> spans = traces.map(path -> Try.call(() -> TracesData.parseFrom(Files.newInputStream(path, READ)))
                        .getOrThrow(RuntimeException::new))
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
                .peek(span -> builder.addEdge(span.parentSpanId, span.spanId))
                .collect(Collectors.toMap(
                        span -> span.spanId,
                        span -> span,
                        (a, b) -> {
                            throw new IllegalStateException("Duplicate span id: " + a.spanId);
                        },
                        LinkedHashMap::new
                ));

        String scenarioTitle = "InstallationE2ETest.fullInstallation";
        String packagePrefix = "devices.configuration.";
        Predicate<Span> scenarioPredicate = span -> span.name().equals(scenarioTitle);
        Predicate<Span> include = span -> true;
        Predicate<Span> exclude = span -> span.name().startsWith("InstallationController.");
        Function<Span, String> callName = span -> span.attribute("code.function").orElseGet(() -> encode(span.name()));
        Function<Span, String> participantName = span -> scenarioPredicate.test(span) ? "scenario" :
                span.anyOfAttributes(Set.of("code.namespace", "db.statement", "http.route"))
                        .map(attribute -> switch (attribute.getKey()) {
                            case "code.namespace" -> {
                                String fullQualifiedClassName = attribute.getValue().toString();
                                if (fullQualifiedClassName.endsWith("Repository")) {
                                    yield "Repository";
                                } else {
                                    yield encode(fullQualifiedClassName
                                            .substring(fullQualifiedClassName.lastIndexOf('.') + 1));
                                }
                            }
                            case "db.statement" -> "Repository";
                            case "http.route" -> "http";
                            default -> encode(span.name());
                        }).orElseGet(() -> switch (span.name()) {
                            case "INSERT", "UPDATE", "SELECT", "DELETE" -> "Repository";
                            case "GET", "PUT", "POST", "PATCH" -> "http"; // "DELETE" is ambiguous
                            case String name &&name.startsWith("Session.") ->"Repository";
                            case String name &&name.startsWith("Transaction.") ->"Repository";
                            case String name &&name.startsWith("SELECT ") ->"Repository";
                            case String name &&name.startsWith("INSERT ") ->"Repository";
                            case String name &&name.startsWith("UPDATE ") ->"Repository";
                            case String name &&name.startsWith("DELETE ") ->"Repository";
                                default -> encode(span.name());
                        });
        Function<Span, String> participantGroup = span -> switch (participantName.apply(span)) {
            case "Repository" -> "persistence";
            case "http" -> "web";
            case String name &&span.attribute("code.namespace")
                    .filter(namespace -> namespace.startsWith(packagePrefix)).isPresent() ->
                encode(span.attribute("code.namespace")
                        .map(namespace -> namespace.substring(packagePrefix.length(), namespace.lastIndexOf('.')))
                        .orElseThrow()
                );
                default -> participantName.apply(span);
        };

        var graph = builder.buildAsUnmodifiable();

        Span scenario = spans.values().stream()
                .filter(scenarioPredicate)
                .findFirst()
                .orElseThrow();
        var calls = new ArrayList<Call>();
        Map<String, LinkedHashSet<String>> participants = new LinkedHashMap<>();
        new DepthFirstIterator<>(graph, scenario.spanId()).forEachRemaining(spanId -> {
            Span span = spans.get(spanId);
            if (span != null) {
                Span parent = spans.get(span.parentSpanId());
                calls.add(Call.of(parent, span, participantName, callName));
            }
        });

        var filtered = calls.stream()
                .sorted(Comparator.comparing(Call::start).reversed())
                .flatMap(call -> {
                    if (call.parent() == null) {
                        return Stream.of(call);
                    } else if (include.test(call.parent()) && !exclude.test(call.parent())
                               && include.test(call.child()) && !exclude.test(call.child())) {
                        return Stream.of(call, call.returning());
                    } else if (include.test(call.child()) && !exclude.test(call.child())) {
                        //look up for better parent
                        return Stream.iterate(call.parent(), span -> span.parentSpanId() != null, span -> spans.get(span.parentSpanId()))
                                .filter(parent -> include.test(parent) && !exclude.test(parent))
                                .findFirst()
                                .map(parent -> Call.of(parent, call.child(), participantName, callName))
                                .stream()
                                .flatMap(betterParentCalls -> Stream.of(betterParentCalls, betterParentCalls.returning()));
                    } else {
                        return Stream.empty();
                    }
                })
                .sorted(Comparator.comparing(Call::start))
                .toList();

        filtered.stream()
                .sorted(Comparator.comparing(Call::start))
                .filter(call -> call.parent() != null)
                .forEach(call -> participants.computeIfAbsent(
                        participantGroup.apply(call.child()),
                        key -> new LinkedHashSet<>()).add(call.childParticipant()));

        System.out.println("sequenceDiagram");
        System.out.println("  participant " + participantName.apply(scenario));
        Stream.of(
                "web",
                "communication",
                "installations",
                "mediators",
                "device",
                "search",
                "persistence"
        ).forEach(group -> {
            LinkedHashSet<String> groupParticipants = participants.get(group);
            if (groupParticipants.size() == 1) {
                String first = groupParticipants.iterator().next();
                if (group.equals(first)) {
                    System.out.println("  participant " + first);
                    return;
                }
            }
            System.out.println("  box " + group);
            groupParticipants.forEach(participant -> System.out.println("    participant " + participant));
            System.out.println("  end");
        });
        filtered.stream()
                .filter(call -> call.parent() != null)
                .forEach(call -> {
                    if (!call.isReturning()) {
                        System.out.println("  " + call.parentParticipant() + " ->>+ " + call.childParticipant() + ": " + call.callName());
                    } else {
                        System.out.println("  " + call.parentParticipant() + " ->>- " + call.childParticipant() + ": " + call.callName());
                    }
                });
    }

    private static void printSequenceDiagramInNaturalOrder(Map<String, LinkedHashSet<String>> participants) {
        System.out.println("sequenceDiagram");
        System.out.println("  participant scenario");
        participants.forEach((group, groupParticipants) -> {
            if (groupParticipants.size() == 1) {
                String first = groupParticipants.iterator().next();
                if (group.equals(first)) {
                    System.out.println("  participant " + first);
                    return;
                }
            }
            System.out.println("  box " + group);
            groupParticipants.forEach(participant -> System.out.println("    participant " + participant));
            System.out.println("  end");
        });
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_16).replace('+', ' ');
    }

    record Call(Span parent, String parentParticipant, Span child, String childParticipant, String callName,
                boolean isReturning) {
        static Call of(Span parent, Span child, Function<Span, String> participantName, Function<Span, String> callName) {
            return new Call(
                    parent,
                    parent != null ? participantName.apply(parent) : null,
                    child,
                    participantName.apply(child),
                    callName.apply(child),
                    false
            );
        }

        Call returning() {
            return new Call(child, childParticipant, parent, parentParticipant, "return " + callName, true);
        }

        Instant start() {
            if (isReturning) {
                return parent.end;
            }
            return child.start;
        }

        public String name() {
            return child().name();
        }
    }

    @Builder
    record Span(
            String name,
            ByteString spanId,
            ByteString parentSpanId,
            Instant start,
            Instant end,
            Map<String, Object> attributes
    ) {

        public String toString(Set<String> attributes) {
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

        @Override
        public int hashCode() {
            return Objects.hash(spanId);
        }

        public Optional<Map.Entry<String, Object>> anyOfAttributes(Set<String> filter) {
            return attributes.entrySet().stream()
                    .filter(entry -> filter.contains(entry.getKey()))
                    .findFirst();
        }

        public Optional<String> attribute(String attribute) {
            return Optional.ofNullable((String) attributes.get(attribute));
        }
    }
}
