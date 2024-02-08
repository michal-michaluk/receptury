package devices.configuration.installations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devices.configuration.communication.BootNotification;
import devices.configuration.device.Location;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DomainEvent.InstallationStarted.class, name = "InstallationStarted_v1"),
        @JsonSubTypes.Type(value = DomainEvent.DeviceAssigned.class, name = "DeviceAssigned_v1"),
        @JsonSubTypes.Type(value = DomainEvent.LocationPredefined.class, name = "LocationPredefined_v1"),
        @JsonSubTypes.Type(value = DomainEvent.BootNotificationProcessed.class, name = "BootNotificationConfirmed_v1"),
        @JsonSubTypes.Type(value = DomainEvent.InstallationCompleted.class, name = "InstallationCompleted_v1"),
})
public sealed interface DomainEvent {
    record InstallationStarted(String orderId, WorkOrder order) implements DomainEvent {
    }

    record DeviceAssigned(String orderId, String deviceId) implements DomainEvent {
    }

    record LocationPredefined(String orderId, String deviceId, Location location) implements DomainEvent {
    }

    record BootNotificationProcessed(String orderId, String deviceId,
                                     BootNotification boot, boolean confirmed) implements DomainEvent {
    }

    record InstallationCompleted(String orderId, String deviceId) implements DomainEvent {
    }
}
