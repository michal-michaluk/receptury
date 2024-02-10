package devices.configuration.installations;

import devices.configuration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static devices.configuration.JsonAssert.assertThat;
import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.InstallationProcessState.State.COMPLETED;
import static devices.configuration.installations.InstallationProcessState.State.DEVICE_ASSIGNED;
import static devices.configuration.installations.InstallationReadModel.QueryParams.params;

@IntegrationTest
class InstallationReadModelTest {

    @Autowired
    InstallationReadModel projection;
    @Autowired
    InstallationReadModel.JpaRepository repo;

    final String orderId = "fixed-device-id";

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void queryById() {
        givenPendingInstallation(orderId);

        Optional<InstallationProcessState> read = projection.queryByOrderId(orderId);

        assertThat(read).isExactlyLike("""
                {
                  "orderId": "fixed-device-id",
                  "deviceId": null,
                  "state": "PENDING"
                }
                """);
    }

    @Test
    void queryAll() {
        givenPendingInstallation("new-order-id");
        givenInstallation(orderId, "device-1", DEVICE_ASSIGNED);
        givenInstallation("other-order-id", "device-2", COMPLETED);

        Page<InstallationProcessState> readWithEmpty = when(params().states(List.of()));

        assertThat(readWithEmpty).isExactlyLike("""
                {
                  "content": [
                    {
                      "orderId": "new-order-id",
                      "deviceId": null,
                      "state": "PENDING"
                    },
                    {
                      "orderId": "fixed-device-id",
                      "deviceId": "device-1",
                      "state": "DEVICE_ASSIGNED"
                    },
                    {
                      "orderId": "other-order-id",
                      "deviceId": "device-2",
                      "state": "COMPLETED"
                    }
                  ],
                  "totalPages": 1,
                  "totalElements": 3,
                  "size": 5000,
                  "page": 0
                }
                """);

        Page<InstallationProcessState> readWithNull = when(params());
        Assertions.assertThat(readWithEmpty).isEqualTo(readWithNull);
    }

    @Test
    @Disabled
    void queryWithState() {
        givenPendingInstallation("new-order-id");
        givenInstallation(orderId, "device-1", DEVICE_ASSIGNED);
        givenInstallation("other-order-id", "device-2", COMPLETED);
        Page<InstallationProcessState> read = when(params().states(List.of(DEVICE_ASSIGNED)));

        assertThat(read).isExactlyLike("""
                {
                  "content": [
                    {
                      "orderId": "fixed-device-id",
                      "deviceId": "device-1",
                      "state": "DEVICE_ASSIGNED"
                    }
                  ],
                  "totalPages": 1,
                  "totalElements": 1,
                  "size": 5000,
                  "page": 0
                }
                """);
    }

    private Page<InstallationProcessState> when(InstallationReadModel.QueryParams.QueryParamsBuilder builder) {
        return transactional(() -> projection.query(builder.build(), Pageable.ofSize(5000)));
    }

    private void givenPendingInstallation(String orderId) {
        transactional(() -> projection.projectionOf(InstallationFixture.state(orderId, null, InstallationProcessState.State.PENDING)));
    }

    private void givenInstallation(String orderId, String deviceId, InstallationProcessState.State state) {
        transactional(() -> projection.projectionOf(InstallationFixture.state(orderId, deviceId, state)));
    }
}
