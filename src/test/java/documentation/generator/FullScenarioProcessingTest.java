package documentation.generator;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import devices.configuration.AppRunner;
import devices.configuration.IntegrationTest;
import devices.configuration.RequestsFixture;
import devices.configuration.auth.AuthFixture;
import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import devices.configuration.installations.InstallationReadModelFixture;
import devices.configuration.installations.InstallationService;
import devices.configuration.mediators.InstallationsToDevicesMediator;
import devices.configuration.mediators.ProtocolsToIntervalsMediator;
import documentation.generator.Mermaid.SequenceDiagram.DiagramParameters;
import documentation.generator.Textual.VerboseScenario;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.core.domain.JavaCodeUnit.Predicates.method;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.InstallationFixture.givenWorkOrderFor;
import static documentation.generator.Step.then;
import static documentation.generator.Step.when;
import static documentation.generator.TelemetryCollector.collectorToTempDirectory;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@IntegrationTest(profiles = {"auth-test", "integration-test"})
@SpringBootTest(classes = AppRunner.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullScenarioProcessingTest {

    private static TelemetryCollector collector;
    private static Path traces;

    @Autowired
    AuthFixture auth;
    @Autowired
    RequestsFixture http;
    @Autowired
    InstallationService service;
    @Autowired
    InstallationsToDevicesMediator installationsToDevicesMediator;
    @Autowired
    ProtocolsToIntervalsMediator protocolsToIntervalsMediator;
    @Autowired
    InstallationReadModelFixture installationReadModelFixture;

    final String deviceId = "device-" + randomAlphanumeric(4);

    @BeforeEach
    void setUp() {
        http.clearJwt();
        http.withJwt(auth.tokenFor("john", "john"));
        preflightToImproveVMPerformance();
        installationReadModelFixture.truncate();
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        new OtelInstrumentationMethodsInclude(
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("devices.configuration"),
                method().and(not(modifier(JavaModifier.PRIVATE))).and(declaredIn(
                        resideInAPackage("devices.configuration..").and(or(
                                simpleNameEndingWith("Service"),
                                simpleNameEndingWith("ReadModel")))
                ))
        ).toPropertyFile(Path.of("src/docs/.otel.properties"));
        var ret = collectorToTempDirectory();
        collector = ret.collector();
        traces = ret.tracesDirectory();
    }

    @AfterAll
    static void afterAll() throws Exception {
        collector.close();
        Thread.sleep(5000);
        generateDocumentation();
    }

    static void generateDocumentation() throws IOException {
        Stream<Path> source = Files.list(traces);
        TelemetrySpans telemetry = TelemetrySources.fromProtoFiles(source);
        PerspectiveParameters parameters = exampleParameters();
        Scenarios scenarios = telemetry.selectScenarios(parameters);
        Perspective perspective = Perspective.perspective(telemetry, scenarios, parameters);
        Printable gantt = new Mermaid.Gantt(perspective, parameters, Mermaid.Gantt.DiagramParameters.defaultParams().build());
        Sink.toFile(Paths.get("src/docs/installation-trace.mmd"))
                .accept(gantt);
        Printable scenario = new VerboseScenario(perspective, parameters);
        Sink.toFile(Paths.get("src/docs/scenario.txt"))
                .accept(scenario);


        var i = new AtomicInteger(0);
        Perspective.Splitting.forEachScenarioStep(telemetry, scenarios, parameters).forEach(stepPerspective -> {
            Printable sequenceDiagram = new Mermaid.SequenceDiagram(stepPerspective, parameters, exampleDiagramParameters());
            Sink.toMarkdown(Paths.get("src/docs/installation-steps.md"), "%%installation-steps-" + i.getAndIncrement())
                    .accept(sequenceDiagram);
        });
    }

    @NotNull
    static PerspectiveParameters exampleParameters() {
        String packagePrefix = "devices.configuration.";
        Predicate<Span> scenarioPredicate = span -> span.attribute("documenting.scenario").isPresent();
        // ||span.attribute("code.function").filter(s -> s.equals("fullInstallationWithoutScenarioDescription")).isPresent();
        // fucked up spans:
        // name: Session\..*
        // name: Transaction\..*
        // name: .*Controller\..*
        return PerspectiveParameters.builder()
                .scenarioPredicate(scenarioPredicate)
                .include(span -> true)
//                .exclude(span -> false)
                .exclude(span -> span.attribute("code.namespace").isEmpty()
                                 || span.attribute("code.namespace").filter(s -> s.contains("$")).isPresent()
                )
//                .exclude(span -> span.name().contains("Controller.") || span.name().contains("Mediator.")
//                                 || Set.of("http", "Repository").contains(participantName.apply(span))
//                )
                .argumentsFilter(span -> span.attributes(Set.of("order", "event", "snapshot", "state", "body")))
                .callName(span -> span.attribute("code.function").orElseGet(span::name))
                .participantName(span -> span.attribute("code.namespace")
                        .map(namespace -> namespace.substring(namespace.lastIndexOf('.') + 1))
                        .orElseGet(span::name))
                .participantGroup(span -> span.attribute("code.namespace")
                        .map(namespace -> namespace.substring(packagePrefix.length(), namespace.lastIndexOf('.')))
                        .orElseGet(span::name)
                )
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

    public static class Actors {
        public static final Actor salesSystem = Actor.system("Sales System");
        public static final Actor device = Actor.device("Device");
        public static final Actor installer = Actor.userRole("Installer");
        public static final Actor maintainer = Actor.userRole("Maintainer");
        public static final Actor devicesDatabase = Actor.database("Devices Database");
    }

    @Test
    @WithSpan
    void fullInstallationWithScenarioDescription() {
        Scenario.title("Device Installation Process")
                .description("The process of installing a device in the system")
                .precondition("Device sold to customer in sales subdomain")
                .precondition("Work order is created for the device installation")
                .includes("Device installation process",
                        "Device configuration",
                        "Communication with device")
                .excludes(
                        "Sales process",
                        "Creation of work order",
                        "Communication to end customer")
                .begin();

        String orderId = "order-" + randomAlphanumeric(4);

        when(Actors.salesSystem, "A work order arrives", () -> {
            Step.highlight("A work order ownership", DeviceFixture.ownership());
            transactional(() -> service.handleWorkOrder(
                    givenWorkOrderFor(orderId, DeviceFixture.ownership())
            ));
        });

        then(Actors.installer, "The work order is pending", () -> {
            http.installations.get(0, 10000).isExactlyLike("""
                    {
                      "content": [
                        {
                          "orderId": "%s",
                          "deviceId": null,
                          "state": "PENDING"
                        }
                      ],
                      "totalPages": 1,
                      "totalElements": 1,
                      "page": 0,
                      "size": 1
                    }
                    """, orderId);

            http.installations.get(orderId).isExactlyLike("""
                    {"orderId":"%s","deviceId":null,"state":"PENDING"}
                    """, orderId);
        });

        when(Actors.installer, "The device is assigned to the work order", () ->
                http.installations.patch(orderId, """
                                { "assignDevice": "%s" }""", deviceId)
                        .isExactlyLike("""
                                {"orderId":"%s","deviceId":"%s","state":"DEVICE_ASSIGNED"}
                                """, orderId, deviceId)
        );

        when(Actors.installer, "The location is assigned to the work order", () ->
                http.installations.patch(orderId, """
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
                        """).isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"DEVICE_ASSIGNED"}
                        """, orderId, deviceId)
        );

        when(Actors.device, "The device boots", () ->
                http.communication.bootIot16(deviceId, """
                        {
                          "chargePointVendor": "Garo",
                          "chargePointModel": "CPF25 Family",
                          "chargePointSerialNumber": "820394A93203",
                          "chargeBoxSerialNumber": "891234A56711",
                          "firmwareVersion": "1.1",
                          "iccid": "112233445566778899C1",
                          "imsi": "082931213347973812",
                          "meterType": "5051",
                          "meterSerialNumber": "937462A48276"
                        }
                        """).hasFieldsLike("""
                        {"interval":1800,"status":"Pending"}
                        """, deviceId)
        );

        then("The device is booted", () ->
                http.installations.get(orderId)
                        .isExactlyLike("""
                                {"orderId":"%s","deviceId":"%s","state":"BOOTED"}
                                """, orderId, deviceId)
        );

        when(Actors.installer, "The boot is confirmed", () ->
                http.installations.patch(orderId, "{ \"confirmBoot\": true }")
                        .isExactlyLike("""
                                {"orderId":"%s","deviceId":"%s","state":"BOOTED"}
                                """, orderId, deviceId)
        );

        when(Actors.installer, "The installation is completed", () ->
                http.installations.patch(orderId, """
                                { "complete": true }""")
                        .isExactlyLike("""
                                {"orderId":"%s","deviceId":"%s","state":"COMPLETED"}
                                """, orderId, deviceId)
        );

        then("The work order is completed", () ->
                http.installations.get(orderId).isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"COMPLETED"}
                        """, orderId, deviceId)
        );

        then(Actors.devicesDatabase, "Device is registered", () -> {
            http.devices.get(deviceId).isExactlyLike("""
                    {
                      "deviceId": "%s",
                      "ownership": {
                        "operator": "Devicex.nl",
                        "provider": "public-devices"
                      },
                      "location": {
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
                      },
                      "openingHours": {
                        "alwaysOpen": true
                      },
                      "settings": {
                        "autoStart": false,
                        "remoteControl": false,
                        "billing": false,
                        "reimbursement": false,
                        "showOnMap": false,
                        "publicAccess": false
                      },
                      "violations": {
                        "operatorNotAssigned": false,
                        "providerNotAssigned": false,
                        "locationMissing": false,
                        "showOnMapButMissingLocation": false,
                        "showOnMapButNoPublicAccess": false
                      },
                      "visibility": {
                        "roamingEnabled": false,
                        "forCustomer": "INACCESSIBLE_AND_HIDDEN_ON_MAP"
                      },
                      "boot": {
                        "protocol": "IoT16",
                        "vendor": "Garo",
                        "model": "CPF25 Family",
                        "serial": "891234A56711",
                        "firmware": "1.1"
                      }
                    }
                    """, deviceId);
        });

        when(Actors.maintainer, "Configure device settings", () ->
                http.devices.patch(deviceId, """
                        {
                          "settings": {
                            "publicAccess": true,
                            "showOnMap": true
                          }
                        }
                        """).hasFieldsLike("""
                        {
                          "deviceId": "%s",
                          "settings": {
                            "showOnMap": true,
                            "publicAccess": true
                          },
                          "visibility": {
                            "forCustomer": "USABLE_AND_VISIBLE_ON_MAP"
                          }
                        }
                        """, deviceId)
        );

        then(Actors.devicesDatabase, "Device is shown on map", () -> {
            http.devices.get(deviceId).hasFieldsLike("""
                    {
                      "deviceId": "%s",
                      "settings": {
                        "showOnMap": true,
                        "publicAccess": true
                      },
                      "visibility": {
                        "forCustomer": "USABLE_AND_VISIBLE_ON_MAP"
                      }
                    }
                    """, deviceId);
        });
    }

    void fullInstallationWithoutScenarioDescription() {

        String orderId = "order-" + randomAlphanumeric(4);

        transactional(() -> service.handleWorkOrder(
                givenWorkOrderFor(orderId, DeviceFixture.ownership())
        ));

        http.installations.get(0, 10000).isExactlyLike("""
                {
                  "content": [
                    {
                      "orderId": "%s",
                      "deviceId": null,
                      "state": "PENDING"
                    }
                  ],
                  "totalPages": 1,
                  "totalElements": 1,
                  "page": 0,
                  "size": 1
                }
                """, orderId);

        http.installations.get(orderId).isExactlyLike("""
                {"orderId":"%s","deviceId":null,"state":"PENDING"}
                """, orderId);

        http.installations.patch(orderId, """
                        { "assignDevice": "%s" }""", deviceId)
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"DEVICE_ASSIGNED"}
                        """, orderId, deviceId);

        http.installations.patch(orderId, """
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
                """).isExactlyLike("""
                {"orderId":"%s","deviceId":"%s","state":"DEVICE_ASSIGNED"}
                """, orderId, deviceId);

        http.communication.bootIot16(deviceId, """
                {
                  "chargePointVendor": "Garo",
                  "chargePointModel": "CPF25 Family",
                  "chargePointSerialNumber": "820394A93203",
                  "chargeBoxSerialNumber": "891234A56711",
                  "firmwareVersion": "1.1",
                  "iccid": "112233445566778899C1",
                  "imsi": "082931213347973812",
                  "meterType": "5051",
                  "meterSerialNumber": "937462A48276"
                }
                """).hasFieldsLike("""
                {"interval":1800,"status":"Pending"}
                """, deviceId);

        http.installations.get(orderId)
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"BOOTED"}
                        """, orderId, deviceId);

        http.installations.patch(orderId, "{ \"confirmBoot\": true }")
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"BOOTED"}
                        """, orderId, deviceId);

        http.installations.patch(orderId, """
                        { "complete": true }""")
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"COMPLETED"}
                        """, orderId, deviceId);

        http.installations.get(orderId).isExactlyLike("""
                {"orderId":"%s","deviceId":"%s","state":"COMPLETED"}
                """, orderId, deviceId);

        http.devices.get(deviceId).isExactlyLike("""
                {
                  "deviceId": "%s",
                  "ownership": {
                    "operator": "Devicex.nl",
                    "provider": "public-devices"
                  },
                  "location": {
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
                  },
                  "openingHours": {
                    "alwaysOpen": true
                  },
                  "settings": {
                    "autoStart": false,
                    "remoteControl": false,
                    "billing": false,
                    "reimbursement": false,
                    "showOnMap": false,
                    "publicAccess": false
                  },
                  "violations": {
                    "operatorNotAssigned": false,
                    "providerNotAssigned": false,
                    "locationMissing": false,
                    "showOnMapButMissingLocation": false,
                    "showOnMapButNoPublicAccess": false
                  },
                  "visibility": {
                    "roamingEnabled": false,
                    "forCustomer": "INACCESSIBLE_AND_HIDDEN_ON_MAP"
                  },
                  "boot": {
                    "protocol": "IoT16",
                    "vendor": "Garo",
                    "model": "CPF25 Family",
                    "serial": "891234A56711",
                    "firmware": "1.1"
                  }
                }
                """, deviceId);

        http.devices.patch(deviceId, """
                {
                  "settings": {
                    "publicAccess": true,
                    "showOnMap": true
                  }
                }
                """).hasFieldsLike("""
                {
                  "deviceId": "%s",
                  "settings": {
                    "showOnMap": true,
                    "publicAccess": true
                  },
                  "visibility": {
                    "forCustomer": "USABLE_AND_VISIBLE_ON_MAP"
                  }
                }
                """, deviceId);

        http.devices.get(deviceId).hasFieldsLike("""
                {
                  "deviceId": "%s",
                  "settings": {
                    "showOnMap": true,
                    "publicAccess": true
                  },
                  "visibility": {
                    "forCustomer": "USABLE_AND_VISIBLE_ON_MAP"
                  }
                }
                """, deviceId);
    }

    private void preflightToImproveVMPerformance() {
        String orderId = "order-" + randomAlphanumeric(4);
        String deviceId = "device-" + randomAlphanumeric(4);
        transactional(() -> service.handleWorkOrder(givenWorkOrderFor(orderId, DeviceFixture.ownership())));
        http.installations.patch(orderId, """
                { "assignDevice": "%s" }""", deviceId);
        http.installations.get(0, 10000);
        installationsToDevicesMediator.create(
                deviceId, DeviceFixture.ownership(), DeviceFixture.location()
        );
        protocolsToIntervalsMediator.heartbeatIntervalFor(CommunicationFixture.boot(deviceId));
        http.devices.get(deviceId);
    }
}
