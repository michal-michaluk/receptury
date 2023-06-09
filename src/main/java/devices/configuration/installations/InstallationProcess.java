package devices.configuration.installations;

import devices.configuration.device.Location;
import devices.configuration.device.Ownership;
import devices.configuration.protocols.BootNotification;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static devices.configuration.installations.DomainEvent.*;

@AllArgsConstructor
@RequiredArgsConstructor
class InstallationProcess {

    private final List<DomainEvent> events;
    private final String orderId;
    private final Ownership ownership;
    private String deviceId;
    private BootNotification boot;
    private Location location;
    private boolean bootConfirmation;
    private boolean finalized;


    static InstallationProcess startInstallationProcessFor(WorkOrder order) {
        Objects.requireNonNull(order.orderId());
        Objects.requireNonNull(order.ownership().operator());
        Objects.requireNonNull(order.ownership().provider());

        return new InstallationProcess(
                new ArrayList<>(),
                order.orderId(),
                order.ownership()
        );
    }

    void assignDevice(String deviceId) {
        Objects.requireNonNull(deviceId);

        ensureProcessIsActive();
        // rules: what if deviceId already is assigned and is differen?
        this.deviceId = deviceId;
        events.add(new DeviceAssigned(orderId, deviceId));
    }

    void assignLocation(Location location) {
        Objects.requireNonNull(location);

        ensureProcessIsActive();
        this.location = location;
        events.add(new LocationPredefined(orderId, deviceId, location));

    }

    void handleBootNotification(BootNotification boot) {
        Objects.requireNonNull(boot);

        if (finalized) return;
        if (!Objects.equals(this.boot, boot)) {
            this.bootConfirmation = false;
        }
        this.boot = boot;
        events.add(new BootNotificationConfirmed(orderId, deviceId, this.boot, bootConfirmation));
    }

    void confirmBootData() {
        if (bootConfirmation) return;
        this.bootConfirmation = true;
        events.add(new BootNotificationConfirmed(orderId, deviceId, boot, bootConfirmation));
    }

    CompletionResult complete() {
        ensureProcessIsActive();
        boolean locationIsDefined = location != null;
        boolean bootIsConfirmed = boot != null && bootConfirmation;
        if (locationIsDefined && bootIsConfirmed) {
            events.add(new InstallationCompleted(orderId, deviceId));
        }
        return new CompletionResult(
                locationIsDefined,
                bootIsConfirmed,
                location,
                ownership
        );
    }

    private void ensureProcessIsActive() {
        if (finalized) throw new IllegalStateException();
    }
}
