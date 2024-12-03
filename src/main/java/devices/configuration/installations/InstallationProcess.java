package devices.configuration.installations;

import devices.configuration.communication.BootNotification;
import devices.configuration.device.Location;
import devices.configuration.device.Ownership;
import devices.configuration.installations.InstallationProcessState.State;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static devices.configuration.installations.DomainEvent.*;

@AllArgsConstructor
class InstallationProcess {

    final List<DomainEvent> events;
    final String orderId;
    String deviceId;
    private Ownership ownership;
    private BootNotification boot;
    private Location location;
    private boolean bootConfirmation;
    private boolean active;

    static InstallationProcess startInstallationProcessFor(WorkOrder order) {
        Objects.requireNonNull(order.orderId());
        assert order.ownership().isOwned();
        var process = new InstallationProcess(
                new ArrayList<>(),
                order.orderId(),
                null,
                order.ownership(),
                null,
                null,
                false,
                true
        );
        process.events.add(new InstallationStarted(order.orderId(), order));
        return process;
    }

    void assignDevice(String deviceId) {
        Objects.requireNonNull(deviceId);
        ensureProcessIsActive();
        if (!Objects.equals(this.deviceId, deviceId)) {
            this.deviceId = deviceId;
            boot = null;
            bootConfirmation = false;
            events.add(new DeviceAssigned(orderId, deviceId));
        }
    }

    void assignLocation(Location location) {
        Objects.requireNonNull(location);

        ensureProcessIsActive();
        this.location = location;
        events.add(new LocationPredefined(orderId, deviceId, location));
    }

    void handleBootNotification(BootNotification boot) {
        Objects.requireNonNull(boot);

        if (!active) return;
        boolean bootConfirmed = this.bootConfirmation && Objects.equals(this.boot, boot);
        this.boot = boot;
        this.bootConfirmation = bootConfirmed;
        events.add(new BootNotificationProcessed(orderId, deviceId, boot, bootConfirmed));
    }

    void confirmBootData() {
        if (bootConfirmation) return;
        this.bootConfirmation = true;
        events.add(new BootNotificationProcessed(orderId, deviceId, boot, bootConfirmation));
    }

    CompletionResult complete() {
        ensureProcessIsActive();
        boolean locationIsDefined = location != null;
        boolean bootIsConfirmed = boot != null && this.bootConfirmation;
        if (locationIsDefined && bootIsConfirmed) {
            active = false;
            events.add(new InstallationCompleted(orderId, deviceId));
        }
        return CompletionResult.builder()
                .locationIsDefined(locationIsDefined)
                .bootIsConfirmed(bootIsConfirmed)
                .location(location)
                .ownership(ownership)
                .build();
    }

    InstallationProcessState asState() {
        return new InstallationProcessState(
                orderId, deviceId, state()
        );
    }

    private State state() {
        if (!active) return State.COMPLETED;
        if (boot != null) return State.BOOTED;
        if (deviceId != null) return State.DEVICE_ASSIGNED;
        return State.PENDING;
    }

    private void ensureProcessIsActive() {
        if (!active) throw new IllegalStateException();
    }
}
