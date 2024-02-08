package devices.configuration.installations;

import devices.configuration.communication.BootNotification;
import devices.configuration.device.Location;
import devices.configuration.device.Ownership;
import devices.configuration.installations.InstallationProcessState.State;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static devices.configuration.installations.DomainEvent.*;

@AllArgsConstructor
@RequiredArgsConstructor
class InstallationProcess {

    final List<DomainEvent> events = new ArrayList<>();
    String orderId;
    String deviceId;
    private Ownership ownership;
    private BootNotification boot;
    private Location location;
    private boolean bootConfirmation;
    private boolean active;

    static InstallationProcess startInstallationProcessFor(WorkOrder order) {
        Objects.requireNonNull(order.orderId());
        assert order.ownership().isOwned();
        var event = new InstallationStarted(order.orderId(), order);
        var process = new InstallationProcess();
        process.handle(event);
        process.events.add(event);
        return process;
    }

    static InstallationProcess fromHistory(List<DomainEvent> history) {
        var process = new InstallationProcess();
        for (DomainEvent event : history)
            switch (event) {
                case InstallationStarted e -> process.handle(e);
                case DeviceAssigned e -> process.handle(e);
                case LocationPredefined e -> process.handle(e);
                case BootNotificationProcessed e -> process.handle(e);
                case InstallationCompleted e -> process.handle(e);
            }
        return process;
    }

    private void handle(InstallationStarted event) {
        this.orderId = event.orderId();
        this.ownership = event.order().ownership();
        this.active = true;
    }

    void assignDevice(String deviceId) {
        Objects.requireNonNull(deviceId);
        ensureProcessIsActive();
        if (!Objects.equals(this.deviceId, deviceId)) {
            var event = new DeviceAssigned(orderId, deviceId);
            handle(event);
            events.add(event);
        }
    }

    private void handle(DeviceAssigned event) {
        this.deviceId = event.deviceId();
        boot = null;
        bootConfirmation = false;
    }

    void assignLocation(Location location) {
        Objects.requireNonNull(location);

        ensureProcessIsActive();
        var event = new LocationPredefined(orderId, deviceId, location);
        handle(event);
        events.add(event);
    }

    private void handle(LocationPredefined event) {
        this.location = event.location();
    }

    void handleBootNotification(BootNotification boot) {
        Objects.requireNonNull(boot);

        if (!active) return;
        boolean bootConfirmed = this.bootConfirmation && Objects.equals(this.boot, boot);
        var event = new BootNotificationProcessed(orderId, deviceId, boot, bootConfirmed);
        handle(event);
        events.add(event);
    }

    private void handle(BootNotificationProcessed event) {
        this.boot = event.boot();
        this.bootConfirmation = event.confirmed();
    }

    void confirmBootData() {
        if (bootConfirmation) return;
        this.bootConfirmation = true;
        var event = new BootNotificationProcessed(orderId, deviceId, boot, bootConfirmation);
        events.add(event);
    }

    CompletionResult complete() {
        ensureProcessIsActive();
        boolean locationIsDefined = location != null;
        boolean bootIsConfirmed = boot != null && this.bootConfirmation;
        if (locationIsDefined && bootIsConfirmed) {
            var event = new InstallationCompleted(orderId, deviceId);
            handle(event);
            events.add(event);
        }
        return CompletionResult.builder()
                .locationIsDefined(locationIsDefined)
                .bootIsConfirmed(bootIsConfirmed)
                .location(location)
                .ownership(ownership)
                .build();
    }

    private void handle(InstallationCompleted event) {
        active = false;
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
