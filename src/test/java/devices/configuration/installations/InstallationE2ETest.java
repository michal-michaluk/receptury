package devices.configuration.installations;

import com.sun.net.httpserver.HttpServer;
import devices.configuration.IntegrationTest;
import devices.configuration.RequestsFixture;
import devices.configuration.auth.AuthFixture;
import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.proto.trace.v1.TracesData;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.InstallationFixture.givenWorkOrderFor;
import static devices.configuration.installations.InstallationProcessState.State.*;
import static java.nio.file.StandardOpenOption.*;

@IntegrationTest(profiles = {"auth-test", "integration-test"})
class InstallationE2ETest {

    @Autowired
    AuthFixture auth;
    @Autowired
    RequestsFixture requests;
    @Autowired
    InstallationService service;

    final String deviceId = DeviceFixture.randomId();

    @BeforeEach
    void setUp() {
        requests.clearJwt();
        requests.withJwt(auth.tokenFor("john", "john"));
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        collector();
    }

    private static void collector() throws IOException {
        var atomic = new AtomicLong(0);
        HttpServer server = HttpServer.create(new InetSocketAddress(43418), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            Path path = Path.of("traces", "trace-" + atomic.getAndIncrement() + ".proto");
            System.out.println("writing to file: " + path.toAbsolutePath());
            try (OutputStream out = Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING)) {
                exchange.getRequestBody().transferTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println(TracesData.parseFrom(exchange.getRequestBody()));
            exchange.close();
        });
        server.start();
        process(Files.list(Path.of("traces")));
    }

    @Test
    void processProto() throws IOException {
        process(Files.list(Path.of("traces")));
    }

    public static void process(Stream<Path> traces) {
        traces.map(path -> Try.call(() -> TracesData.parseFrom(Files.newInputStream(path, READ)))
                        .getOrThrow(RuntimeException::new))
//                .flatMap(trace -> trace.getResourceSpansList().stream())
//                .flatMap(trace -> trace.getScopeSpansList().stream())
//                .flatMap(trace -> trace.getSpansList().stream())
//                .map(span -> span.getName())
                .forEach(System.out::println);
    }

    @Test
    @WithSpan
    void fullInstallation() {
        WorkOrder order = givenWorkOrderFor(DeviceFixture.ownership());

        transactional(() -> service.handleWorkOrder(order));
        get().contains(state(order, PENDING));

        get(order)
                .isEqualTo(state(order, PENDING));

        patch(order, "{ \"assignDevice\": \"%s\" }".formatted(deviceId))
                .isEqualTo(state(order, deviceId, DEVICE_ASSIGNED));

        patch(order, """
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
                .isEqualTo(state(order, deviceId, DEVICE_ASSIGNED));

        transactional(() -> service.handleBootNotification(CommunicationFixture.boot(deviceId)));
        get(order)
                .isEqualTo(state(order, deviceId, BOOTED));

        patch(order, "{ \"confirmBoot\": true }")
                .isEqualTo(state(order, deviceId, BOOTED));

        patch(order, "{ \"complete\": true }")
                .isEqualTo(state(order, deviceId, COMPLETED));
    }

    private ObjectAssert<InstallationProcessState> get(WorkOrder order) {
        return Assertions.assertThat(requests.installations.get(order.orderId()));
    }

    private IterableAssert<InstallationProcessState> get() {
        return Assertions.assertThat(requests.installations.get(0, 10000));
    }

    private ObjectAssert<InstallationProcessState> patch(WorkOrder order, @Language("JSON") String body) {
        return Assertions.assertThat(requests.installations.patch(order.orderId(), body));
    }

    @NotNull
    private static InstallationProcessState state(WorkOrder order, InstallationProcessState.State state) {
        return InstallationFixture.state(order.orderId(), null, state);
    }

    @NotNull
    private static InstallationProcessState state(WorkOrder order, String deviceId, InstallationProcessState.State state) {
        return InstallationFixture.state(order.orderId(), deviceId, state);
    }
}
