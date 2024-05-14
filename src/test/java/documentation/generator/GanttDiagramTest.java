package documentation.generator;

import documentation.generator.Mermaid.Gantt.DiagramParameters;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

class GanttDiagramTest {


    @Test
    void processProto() throws IOException {
        Stream<Path> source = Files.list(Path.of("src/test/resources/traces/devices-e2e"));
        TelemetrySpans telemetry = TelemetrySources.fromProtoFiles(source);
        List<PerspectiveParameters> perspectives = List.of(exampleParameters());
        perspectives.forEach(parameters -> {
            Scenarios scenarios = telemetry.selectScenarios(parameters);
            Perspective perspective = Perspective.perspective(telemetry, scenarios, parameters);
            Printable diagram = new Mermaid.Gantt(perspective, parameters, exampleDiagramParameters());
            Sink.toNewFile(Paths.get("src/docs/installation-gantt.mmd"))
                    .accept(diagram);
        });
    }

    @NotNull
    static PerspectiveParameters exampleParameters() {
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
                                    yield fullQualifiedClassName
                                            .substring(fullQualifiedClassName.lastIndexOf('.') + 1);
                                }
                            }
                            case "db.statement" -> "Repository";
                            case "http.route" -> "http";
                            default -> span.name();
                        }).orElseGet(() -> switch (span.name()) {
                            case "INSERT", "UPDATE", "SELECT", "DELETE" -> "Repository";
                            case "GET", "PUT", "POST", "PATCH" -> "http"; // "DELETE" is ambiguous
                            case String name &&name.startsWith("Session.") ->"Repository";
                            case String name &&name.startsWith("Transaction.") ->"Repository";
                            case String name &&name.startsWith("SELECT ") ->"Repository";
                            case String name &&name.startsWith("INSERT ") ->"Repository";
                            case String name &&name.startsWith("UPDATE ") ->"Repository";
                            case String name &&name.startsWith("DELETE ") ->"Repository";
                                default -> span.name();
                        });

        return PerspectiveParameters.builder()
                .scenarioPredicate(scenarioPredicate)
                .include(span -> true)
                .exclude(span -> span.name().startsWith("InstallationController.")
                                 || Set.of("http", "Repository", "InstallationsToDevicesMediator").contains(participantName.apply(span))
                )
                .callName(span -> span.attribute("code.function").orElseGet(span::name))
//                .argumentsFilter(span -> Stream.of())
                .argumentsFilter(span -> span.attributes(Set.of("order", "event", "snapshot", "state", "body")))
                .participantName(participantName)
                .participantGroup(span -> switch (participantName.apply(span)) {
                    case "Repository" -> "persistence";
                    case "http" -> "web";
                    case String name &&span.attribute("code.namespace")
                            .filter(namespace -> namespace.startsWith(packagePrefix)).isPresent() ->
                        span.attribute("code.namespace")
                                .map(namespace -> namespace.substring(packagePrefix.length(), namespace.lastIndexOf('.')))
                                .orElseThrow();
                        default -> participantName.apply(span);
                })
                .build();
    }

    private static DiagramParameters exampleDiagramParameters() {
        return DiagramParameters.defaultParams()
                .title("Device Installation process")
                .build();
    }
}
