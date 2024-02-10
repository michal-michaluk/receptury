package devices.configuration.installations;

import devices.configuration.IntegrationTest;
import devices.configuration.RequestsFixture;
import devices.configuration.auth.AuthFixture;
import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.InstallationFixture.givenWorkOrderFor;
import static devices.configuration.installations.InstallationProcessState.State.*;

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

    @WithSpan
    private ObjectAssert<InstallationProcessState> get(WorkOrder order) {
        return Assertions.assertThat(requests.installations.get(order.orderId()));
    }

    @WithSpan
    private IterableAssert<InstallationProcessState> get() {
        return Assertions.assertThat(requests.installations.get(0, 10000));
    }

    @WithSpan
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
