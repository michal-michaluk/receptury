package devices.configuration.installations;

import devices.configuration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static devices.configuration.JsonAssert.assertThat;
import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.ProcessFixture.given;

@IntegrationTest
class InstallationEventSourcingRepositoryTest {

    @Autowired
    InstallationEventSourcingRepository repository;

    @Test
    void noProcessFoundByOrderId() {
        Assertions.assertThatThrownBy(() -> transactional(
                () -> repository.getByOrderId("not-existing")
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noProcessFoundByDeviceId() {
        Assertions.assertThat(transactional(
                () -> repository.getByDeviceId("not-existing")
        )).isEmpty();
    }

    @Test
    void loadNewProcessByOrderId() {
        InstallationProcess process = given().newProcess();

        whenProcessIsSaved(process);

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void loadProcessByDeviceId() {
        InstallationProcess process = given().withDeviceAssigned();

        whenProcessIsSaved(process);

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    @Test
    void loadAlmostCompletedProcessByDeviceId() {
        InstallationProcess process = given().almostCompleted();

        whenProcessIsSaved(process);

        assertThat(transactional(() -> repository.getByOrderId(process.orderId)))
                .isExactlyLike(process);
    }

    private void whenProcessIsSaved(InstallationProcess process) {
        transactional(() -> repository.save(process));
    }
}
