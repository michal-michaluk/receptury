package devices.configuration.installations;

import devices.configuration.device.DeviceFixture;
import devices.configuration.device.Ownership;
import devices.configuration.installations.DomainEvent.*;
import devices.configuration.installations.InstallationProcessState.State;
import devices.configuration.protocols.BootNotification;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static devices.configuration.installations.InstallationProcessAssert.assertThat;
import static devices.configuration.protocols.BootNotificationFixture.boot;
import static org.assertj.core.api.Assertions.assertThat;

class InstallationProcessTest {

    private final ProcessFixture fixture = ProcessFixture.given();

    @Test
    void validStartOfInstallationProcess() {
        var order = givenWorkOrderFor(DeviceFixture.ownership());

        var process = InstallationProcess.startInstallationProcessFor(order);

        assertThat(process)
                .emittedEvents(InstallationStarted.class)
                .isInState(State.PENDING);
    }

    @Test
    void cannotStartUnownedInstallationProcess() {
        var order = givenWorkOrderFor(Ownership.unowned());

        Assertions.assertThatThrownBy(() ->
                        InstallationProcess.startInstallationProcessFor(order))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void assignDevice() {
        var process = fixture.newProcess();

        process.assignDevice(fixture.deviceId);

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class
                )
                .isInState(State.DEVICE_ASSIGNED);
    }

    @Test
    void reassignDevice() {
        var process = fixture.withDeviceAssigned();

        process.assignDevice("other-device-Id");

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        DeviceAssigned.class
                )
                .isInState(State.DEVICE_ASSIGNED)
                .hasDeviceAssigned("other-device-Id");
    }

    @Test
    void assignLocation() {
        var process = fixture.withDeviceAssigned();

        process.assignLocation(DeviceFixture.location());


        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        LocationPredefined.class
                )
                .isInState(State.DEVICE_ASSIGNED);
    }

    @Test
    void handleBootNotification() {
        var process = fixture.withDeviceAssigned();

        process.handleBootNotification(boot(fixture.deviceId));

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        BootNotificationProcessed.class
                )
                .isInState(State.BOOTED)
                .bootIsNotConfirmed();
    }

    @Test
    void confirmBootData() {
        var process = fixture.withDeviceAssigned();

        process.handleBootNotification(boot(fixture.deviceId));
        process.confirmBootData();

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class
                )
                .isInState(State.BOOTED)
                .bootIsConfirmed();
    }

    @Test
    void sameBootKeepsBootConfirmation() {
        var process = fixture.withDeviceAssigned();

        process.handleBootNotification(boot(fixture.deviceId));
        process.confirmBootData();
        process.handleBootNotification(boot(fixture.deviceId));

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class
                )
                .isInState(State.BOOTED)
                .bootIsConfirmed();
    }

    @Test
    void differentBootResetsBootConfirmation() {
        var process = fixture.withDeviceAssigned();

        process.handleBootNotification(boot(fixture.deviceId));
        process.confirmBootData();

        BootNotification newBoot = boot(fixture.deviceId).toBuilder()
                .firmware("updated-firmware")
                .build();
        process.handleBootNotification(newBoot);

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class
                )
                .isInState(State.BOOTED)
                .bootIsNotConfirmed();
    }

    @Test
    void reassignedDeviceResetsBoot() {
        var process = fixture.withDeviceAssigned();

        process.handleBootNotification(boot(fixture.deviceId));
        process.confirmBootData();
        process.assignDevice("other-device");

        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class,
                        DeviceAssigned.class
                )
                .isInState(State.DEVICE_ASSIGNED)
                .bootIsNotConfirmed();
    }

    @Test
    void completeSuccessfully() {
        var process = fixture.almostCompleted();

        CompletionResult result = process.complete();

        assertThat(result).isEqualTo(
                CompletionResult.builder()
                        .locationIsDefined(true)
                        .bootIsConfirmed(true)
                        .location(DeviceFixture.location())
                        .ownership(DeviceFixture.ownership())
                        .build()
        );
        assertThat(process)
                .emittedEvents(
                        InstallationStarted.class,
                        DeviceAssigned.class,
                        BootNotificationProcessed.class,
                        BootNotificationProcessed.class,
                        LocationPredefined.class,
                        InstallationCompleted.class
                )
                .isInState(State.COMPLETED);
    }

    @Test
    void completeWithoutBoot() {
        var process = fixture.withDeviceAssigned();
        process.assignLocation(DeviceFixture.location());

        CompletionResult result = process.complete();

        assertThat(result).isEqualTo(
                CompletionResult.builder()
                        .locationIsDefined(true)
                        .location(DeviceFixture.location())
                        .bootIsConfirmed(false)
                        .ownership(DeviceFixture.ownership())
                        .build()
        );
        assertThat(process)
                .isInState(State.DEVICE_ASSIGNED);
    }

    @Test
    void completeWithoutBootConfirmation() {
        var process = fixture.withDeviceAssigned();
        process.assignLocation(DeviceFixture.location());
        process.handleBootNotification(boot(fixture.deviceId));

        CompletionResult result = process.complete();

        assertThat(result).isEqualTo(
                CompletionResult.builder()
                        .locationIsDefined(true)
                        .location(DeviceFixture.location())
                        .bootIsConfirmed(false)
                        .ownership(DeviceFixture.ownership())
                        .build()
        );
        assertThat(process)
                .isInState(State.BOOTED);
    }

    @Test
    void completeWithoutLocation() {
        var process = fixture.withDeviceAssigned();
        process.handleBootNotification(boot(fixture.deviceId));
        process.confirmBootData();

        CompletionResult result = process.complete();

        assertThat(result).isEqualTo(
                CompletionResult.builder()
                        .locationIsDefined(false)
                        .location(null)
                        .bootIsConfirmed(true)
                        .ownership(DeviceFixture.ownership())
                        .build()
        );
        assertThat(process)
                .isInState(State.BOOTED);
    }

    @NotNull
    private static WorkOrder givenWorkOrderFor(@NotNull Ownership ownership) {
        return new WorkOrder(UUID.randomUUID().toString(), ownership);
    }

}
