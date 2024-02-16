package documentation.generator;

import devices.configuration.AppRunner;
import devices.configuration.IntegrationTest;
import devices.configuration.RequestsFixture;
import devices.configuration.auth.AuthFixture;
import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import devices.configuration.installations.InstallationFixture;
import devices.configuration.installations.InstallationProcessState;
import devices.configuration.installations.InstallationService;
import documentation.generator.Mermaid.SequenceDiagram.DiagramParameters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.InstallationFixture.givenWorkOrderFor;
import static devices.configuration.installations.InstallationProcessState.State.*;
import static documentation.generator.Step.then;
import static documentation.generator.Step.when;
import static documentation.generator.TelemetryCollector.collectorToTempDirectory;

@IntegrationTest(profiles = {"auth-test", "integration-test"})
@SpringBootTest(classes = AppRunner.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullScenarioProcessingTest {

    private static TelemetryCollector collector;
    private static Path traces;

    @Autowired
    AuthFixture auth;
    @Autowired
    RequestsFixture requests;
    @Autowired
    InstallationService service;

    final String deviceId = "device-2";

    static void generateDocumentation() throws IOException {
        Stream<Path> source = Files.list(traces);
        TelemetrySpans telemetry = TelemetrySources.fromProtoFiles(source);
        List<PerspectiveParameters> perspectives = List.of(exampleParameters());
        perspectives.forEach(parameters -> {
            Scenarios scenarios = telemetry.selectScenarios(parameters);
            Perspective perspective = Perspective.perspective(telemetry, scenarios, parameters);
            Printable sequenceDiagram = new Mermaid.SequenceDiagram(perspective, parameters, exampleDiagramParameters());
            Sink.toFile(Paths.get("src/docs/installation-sequence.mmd"))
                    .accept(sequenceDiagram);
            Printable gantt = new Mermaid.Gantt(perspective, parameters, Mermaid.Gantt.DiagramParameters.defaultParams().build());
            Sink.toFile(Paths.get("src/docs/installation-trace.mmd"))
                    .accept(gantt);
        });
    }

    @BeforeEach
    void setUp() {
        requests.clearJwt();
        requests.withJwt(auth.tokenFor("john", "john"));
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        var ret = collectorToTempDirectory();
        collector = ret.collector();
        traces = ret.tracesDirectory();
        System.out.println("traces dir:" + traces);
    }

    @AfterAll
    static void afterAll() throws Exception {
        collector.close();
        Thread.sleep(5000);
        generateDocumentation();
    }

    public class Actors {
        public static final Actor salesSystem = Actor.system("Sales System");
        public static final Actor device = Actor.device("Device");
        public static final Actor installer = Actor.userRole("Installer");
        public static final Actor devicesDatabase = Actor.database("Devices Database");
    }

    Scenario scenario = Scenario.title("Device Installation Process")
            .description("The process of installing a device in the system")
            .precondition("Device sold to customer in sales subdomain")
            .precondition("Work order is created for the device installation")
            .includes("Device installation process",
                    "Device configuration",
                    "Communication with device")
            .excludes(
                    "Sales process",
                    "Creation of work order",
                    "Communication to end customer");

    @Test
    @WithSpan
    void fullInstallation() {
        scenario.begin();
        String orderId = "order-1";

        when(Actors.salesSystem, "A work order arrives", () ->
                transactional(() -> service.handleWorkOrder(
                        Step.highlight("Work order", givenWorkOrderFor(orderId, DeviceFixture.ownership()))
                ))
        );

        then("The work order is pending", () -> {
            get().contains(state(orderId, PENDING));

            get(orderId)
                    .isEqualTo(state(orderId, PENDING));
        });

        when(Actors.installer, "The device is assigned to the work order", () ->
                patch(orderId, "{ \"assignDevice\": \"%s\" }".formatted(deviceId))
                        .isEqualTo(state(orderId, deviceId, DEVICE_ASSIGNED))
        );

        when(Actors.installer, "The location is assigned to the work order", () ->
                patch(orderId, """
                        {
                          "assignLocation": {
                            "street": "Rakietowa",
                            "houseNumber": "1A",
                            "city": "Wrocław",
                            "postalCode": "54-621",
                            "state": null,
                            "country": "POL",
                            "coordinates": {
                              "longitude": 51.09836221719513,
                              "latitude": 16.931752852309156
                            }
                          }
                        }
                        """)
                        .isEqualTo(state(orderId, deviceId, DEVICE_ASSIGNED))
        );

        when(Actors.device, "The device is booted", () ->
                transactional(() -> service.handleBootNotification(
                        Step.highlight("Boot notification", CommunicationFixture.boot(deviceId))))
        );

        then("The device is booted", () ->
                get(orderId)
                        .isEqualTo(state(orderId, deviceId, BOOTED))
        );

        when(Actors.installer, "The boot is confirmed", () ->
                patch(orderId, "{ \"confirmBoot\": true }")
                        .isEqualTo(state(orderId, deviceId, BOOTED))
        );

        when(Actors.installer, "The installation is completed", () ->
                patch(orderId, "{ \"complete\": true }")
                        .isEqualTo(state(orderId, deviceId, COMPLETED))
        );
    }

    private ObjectAssert<InstallationProcessState> get(String orderId) {
        return Assertions.assertThat(requests.installations.get(orderId));
    }

    private IterableAssert<InstallationProcessState> get() {
        return Assertions.assertThat(requests.installations.get(0, 10000));
    }

    private ObjectAssert<InstallationProcessState> patch(String orderId, @Language("JSON") String body) {
        return Assertions.assertThat(requests.installations.patch(orderId, body));
    }

    @NotNull
    private static InstallationProcessState state(String orderId, InstallationProcessState.State state) {
        return InstallationFixture.state(orderId, null, state);
    }

    @NotNull
    private static InstallationProcessState state(String orderId, String deviceId, InstallationProcessState.State state) {
        return InstallationFixture.state(orderId, deviceId, state);
    }

    @NotNull
    static PerspectiveParameters exampleParameters() {
        String scenarioTitle = "FullScenarioProcessingTest.fullInstallation";
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
        return DiagramParameters.builder()
                .participantsGroupsOrder(List.of(
                        "web",
                        "installations",
                        "mediators",
                        "device",
                        "communication",
                        "search",
                        "persistence"))
                .build();
    }
}
