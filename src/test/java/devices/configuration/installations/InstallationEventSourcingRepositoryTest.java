package devices.configuration.installations;

import devices.configuration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static devices.configuration.JsonAssert.assertThat;
import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.ProcessFixture.given;

@Transactional
@IntegrationTest
class InstallationEventSourcingRepositoryTest {

    @Autowired
    InstallationEventSourcingRepository repository;

    @Test
    void noProcessFoundByOrderId() {
        Assertions.assertThatThrownBy(
                        () -> transactional(() -> repository.getByOrderId("not-existing")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noProcessFoundByDeviceId() {
        Assertions.assertThatThrownBy(
                        () -> transactional(() -> repository.getByDeviceId("not-existing")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void loadNewProcessByOrderId() {
        InstallationProcess process = given().newProcess();

        transactional(() -> repository.save(process));

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void loadProcessByDeviceId() {
        InstallationProcess process = given().withDeviceAssigned();

        transactional(() -> repository.save(process));

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void loadAlmostCompletedProcessByDeviceId() {
        InstallationProcess process = given().almostCompleted();

        transactional(() -> repository.save(process));

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void hideCompletedProcessByOrderId() {
        InstallationProcess process = given().completed();

        transactional(() -> repository.save(process));

        Assertions.assertThatThrownBy(
                        () -> transactional(() -> repository.getByOrderId(process.orderId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hideCompletedProcessByDeviceId() {
        InstallationProcess process = given().completed();

        transactional(() -> repository.save(process));

        Assertions.assertThatThrownBy(
                        () -> transactional(() -> repository.getByDeviceId(process.deviceId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
