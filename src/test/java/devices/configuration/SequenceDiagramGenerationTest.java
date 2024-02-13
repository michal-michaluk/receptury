package devices.configuration;

import com.google.protobuf.ByteString;
import devices.configuration.SequenceDiagramGenerationTest.Mermaid.SequenceDiagram.DiagramParameters;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.TracesData;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static devices.configuration.SequenceDiagramGenerationTest.Mermaid.SequenceDiagram.encode;
import static java.nio.file.StandardOpenOption.READ;

public class SequenceDiagramGenerationTest {

    @Test
    void processProto() throws IOException {
        process(Files.list(Path.of("src/test/resources/traces/devices-installation")));
    }

    public static void process(Stream<Path> traces) {
        TelemetrySpans telemetry = Telemetry.fromProtoFiles(traces);
        Parameters parameters = exampleParameters();
        DiagramParameters diagramParameters = exampleDiagramParameters();
        Scenarios scenarios = Scenarios.selectScenarios(telemetry, parameters);
        scenarios.forEach(scenario -> {
            Printable diagram = new Mermaid.SequenceDiagram(scenario, parameters, diagramParameters);
            Path path = Paths.get("src/docs/" + scenario.root.name() + "-sequence.mmd");
            saveToFile(path, diagram);
        });
    }

    private static void saveToFile(Path path, Printable diagram) {
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedWriter file = Files.newBufferedWriter(path);
             PrintWriter writer = new PrintWriter(file)) {

            try (PrintWriter out = new PrintWriter(writer)) {
                diagram.print(out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Parameters exampleParameters() {
        String scenarioTitle = "InstallationE2ETest.fullInstallation";
        String packagePrefix = "devices.configuration.";
        Predicate<Span> scenarioPredicate = span -> span.name().equals(scenarioTitle);
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

        return Parameters.builder()
                .scenarioPredicate(scenarioPredicate)
                .include(span -> true)
                .exclude(span -> span.name().startsWith("InstallationController.")
                                 || Set.of("http", "Repository", "InstallationsToDevicesMediator").contains(participantName.apply(span))
                )
                .callName(span -> span.attribute("code.function").orElseGet(() -> encode(span.name())))
                .argumentsFilter(span -> Stream.of())
                //Function<Span, Stream<Map.Entry<String, Object>>> argumentsFilter = span -> span.attributes(Set.of("order"));
                .participantName(participantName)
                .participantGroup(span -> switch (participantName.apply(span)) {
                    case "Repository" -> "persistence";
                    case "http" -> "web";
                    case String name &&span.attribute("code.namespace")
                            .filter(namespace -> namespace.startsWith(packagePrefix)).isPresent() ->
                        encode(span.attribute("code.namespace")
                                .map(namespace -> namespace.substring(packagePrefix.length(), namespace.lastIndexOf('.')))
                                .orElseThrow()
                        );
                        default -> participantName.apply(span);
                })
                .build();
    }

    private static DiagramParameters exampleDiagramParameters() {
        return DiagramParameters.builder()
                .participantsGroupsOrder(List.of(
                        "web",
                        "installations",
                        "communication",
                        "mediators",
                        "device",
                        "search",
                        "persistence"))
                .build();
    }

    @Builder
    public record Parameters(
            Predicate<Span> scenarioPredicate,
            Predicate<Span> include,
            Predicate<Span> exclude,
            Function<Span, String> callName,
            Function<Span, Stream<Map.Entry<String, Object>>> argumentsFilter,
            Function<Span, String> participantName,
            Function<Span, String> participantGroup
    ) {}

    public record Scenarios(Parameters parameters, List<Scenario> scenarios) {
        public record Scenario(Span root, List<Call> calls, LinkedHashMap<String, LinkedHashSet<String>> participants) {
            public LinkedHashSet<String> participantGroup(String group) {
                LinkedHashSet<String> groupParticipants = participants.get(group);
                return Objects.requireNonNullElseGet(groupParticipants, LinkedHashSet::new);
            }
        }

        public void forEach(Consumer<Scenario> consumer) {
            scenarios.forEach(consumer);
        }

        @NotNull
        public static Scenarios selectScenarios(TelemetrySpans telemetry, Parameters parameters) {
            List<Scenario> list = telemetry.spans().values().stream()
                    .filter(parameters.scenarioPredicate())
                    .map(scenario -> {
                        var subGraph = scenarioSubGraph(telemetry, parameters, scenario);
                        var filtered = filterAndSortCalls(telemetry, parameters, subGraph);
                        var participants = filtered.stream()
                                .filter(call -> call.parent() != null)
                                .collect(Collectors.groupingBy(
                                        call -> parameters.participantGroup().apply(call.child()),
                                        LinkedHashMap::new, Collectors.mapping(Call::childParticipant, Collectors.toCollection(LinkedHashSet::new))
                                ));
                        return new Scenario(scenario, filtered, participants);
                    }).toList();

            return new Scenarios(parameters, list);
        }

        @NotNull
        private static List<Call> scenarioSubGraph(TelemetrySpans telemetry, Parameters parameters, Span scenario) {
            var scenarioSubGraph = new ArrayList<Call>();
            new DepthFirstIterator<>(telemetry.graph(), scenario.spanId()).forEachRemaining(spanId -> {
                Span span = telemetry.span(spanId);
                if (span != null) {
                    Span parent = telemetry.span(span.parentSpanId());
                    scenarioSubGraph.add(Call.of(parent, span, parameters.participantName(), parameters.callName()));
                }
            });
            return scenarioSubGraph;
        }

        @NotNull
        private static List<Call> filterAndSortCalls(TelemetrySpans telemetry, Parameters parameters, List<Call> subGraph) {
            return subGraph
                    .stream()
                    .sorted(Comparator.comparing(Call::start).reversed())
                    .flatMap(call -> {
                        if (call.parent() == null) {
                            return Stream.of(call);
                        } else if (parameters.include().test(call.parent()) && !parameters.exclude().test(call.parent())
                                   && parameters.include().test(call.child()) && !parameters.exclude().test(call.child())) {
                            return Stream.of(call);
                        } else if (parameters.include().test(call.child()) && !parameters.exclude().test(call.child())) {
                            //look up for better parent
                            return Stream.iterate(call.parent(), span -> span.parentSpanId() != null, span -> telemetry.span(span.parentSpanId()))
                                    .filter(parent -> parameters.include().test(parent) && !parameters.exclude().test(parent))
                                    .findFirst()
                                    .map(parent -> Call.of(parent, call.child(), parameters.participantName(), parameters.callName()))
                                    .stream();
                        } else {
                            return Stream.empty();
                        }
                    })
                    .flatMap(Call::includeReturning)
                    .sorted(Comparator.comparing(Call::start))
                    .toList();
        }
    }

    record Call(Span parent, String parentParticipant, Span child, String childParticipant, String callName,
                Option variant) {

        enum Option {
            INITIATING, RETURNING, SELF_CALL
        }

        static Call of(Span parent, Span child, Function<Span, String> participantName, Function<Span, String> callName) {
            String parentParticipant = parent != null ? participantName.apply(parent) : null;
            String childParticipant = participantName.apply(child);
            Option option = childParticipant.equals(parentParticipant) ? Option.SELF_CALL : Option.INITIATING;
            return new Call(
                    parent,
                    parentParticipant,
                    child,
                    childParticipant,
                    callName.apply(child),
                    option
            );
        }

        Instant start() {
            if (variant == Option.RETURNING) {
                return parent.end;
            }
            return child.start;
        }

        Stream<Call> includeReturning() {
            if (parent() == null || variant == Option.SELF_CALL) {
                return Stream.of(this);
            }
            return Stream.of(this, returning());
        }

        private Call returning() {
            return new Call(
                    child, childParticipant,
                    parent, parentParticipant,
                    "return " + callName,
                    Option.RETURNING
            );
        }

        @Override
        public String toString() {
            return "call:" +
                   "\n from: " + parentParticipant +
                   "\n to: " + childParticipant +
                   "\n call'" + callName + " " + variant +
                   "\n start: " + child.start() +
                   "\n end: " + child.end();
        }
    }

    public interface Printable {
        void print(PrintWriter out);
    }

    public static class Mermaid {
        public record SequenceDiagram(
                Scenarios.Scenario scenario,
                Parameters parameters,
                DiagramParameters diagramParameters
        ) implements Printable {
            @Builder
            public record DiagramParameters(List<String> participantsGroupsOrder) {
            }

            @Override
            public void print(PrintWriter out) {
                out.println("sequenceDiagram");
                out.println("  participant " + parameters.participantName().apply(scenario.root()));
                diagramParameters.participantsGroupsOrder()
                        .forEach(group -> {
                            var groupParticipants = scenario.participantGroup(group);
                            if (groupParticipants.size() == 1) {
                                String first = groupParticipants.iterator().next();
                                if (group.equals(first)) {
                                    out.println("  participant " + first);
                                    return;
                                }
                            }
                            out.println("  box " + group);
                            groupParticipants.forEach(participant -> out.println("    participant " + participant));
                            out.println("  end");
                        });
                scenario.calls().stream()
                        .filter(call -> call.parent() != null)
                        .forEach(call -> {
                            switch (call.variant) {
                                case INITIATING -> {
                                    out.println("  " + call.parentParticipant() + " ->>+ " + call.childParticipant() + ": " + call.callName());
                                    parameters.argumentsFilter().apply(call.child())
                                            .forEach(arguments -> out.println("  Note left of " + call.childParticipant() + ": " + encode(arguments.getKey()) + " = " + encode(arguments.getValue().toString())));
                                }
                                case RETURNING ->
                                        out.println("  " + call.parentParticipant() + " ->>- " + call.childParticipant() + ": " + call.callName());
                                case SELF_CALL ->
                                        out.println("  " + call.parentParticipant() + " ->> " + call.childParticipant() + ": " + call.callName());
                            }
                        });
            }

            public static String encode(String value) {
                return URLEncoder.encode(value, StandardCharsets.UTF_16).replace('+', ' ');
            }
        }
    }

    public static class Telemetry {
        @NotNull
        public static SequenceDiagramGenerationTest.TelemetrySpans fromProtoFiles(Stream<Path> traces) {
            Stream<TracesData> tracesDataStream = traces
                    .map(path -> Try.call(() -> TracesData.parseFrom(Files.newInputStream(path, READ)))
                            .getOrThrow(RuntimeException::new));

            return fromProtoTracesData(tracesDataStream);
        }

        @NotNull
        public static SequenceDiagramGenerationTest.TelemetrySpans fromProtoTracesData(Stream<TracesData> tracesDataStream) {
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
                    .peek(span -> graph.addEdge(span.parentSpanId, span.spanId))
                    .collect(Collectors.toMap(
                            span -> span.spanId,
                            span -> span,
                            (a, b) -> {
                                throw new IllegalStateException("Duplicate span id: " + a.spanId);
                            },
                            LinkedHashMap::new
                    ));
            return new TelemetrySpans(spans, graph.buildAsUnmodifiable());
        }
    }

    public record TelemetrySpans(Map<ByteString, Span> spans, Graph<Object, DefaultEdge> graph) {
        public Span span(Object spanId) {
            return spans.get(spanId);
        }
    }

    @Builder
    public record Span(
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

        public Optional<Map.Entry<String, Object>> anyOfAttributes(Set<String> filter) {
            return attributes.entrySet().stream()
                    .filter(entry -> filter.contains(entry.getKey()))
                    .findFirst();
        }

        public Stream<Map.Entry<String, Object>> attributes(Set<String> filter) {
            return attributes.entrySet().stream()
                    .filter(entry -> filter.contains(entry.getKey()));
        }

        public Optional<String> attribute(String attribute) {
            return Optional.ofNullable((String) attributes.get(attribute));
        }

        @Override
        public int hashCode() {
            return Objects.hash(spanId);
        }
    }
}
