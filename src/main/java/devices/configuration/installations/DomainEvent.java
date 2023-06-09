package devices.configuration.installations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devices.configuration.device.Location;
import devices.configuration.protocols.BootNotification;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DomainEvent.InstallationStarted.class, name = "InstallationStarted_v1"),
        @JsonSubTypes.Type(value = DomainEvent.DeviceAssigned.class, name = "DeviceAssigned_v1"),
        @JsonSubTypes.Type(value = DomainEvent.LocationPredefined.class, name = "LocationPredefined_v1"),
        @JsonSubTypes.Type(value = DomainEvent.BootNotificationConfirmed.class, name = "BootNotificationConfirmed_v1"),
        @JsonSubTypes.Type(value = DomainEvent.InstallationCompleted.class, name = "InstallationCompleted_v1"),
})
interface DomainEvent {
    record InstallationStarted(String workOrderId, WorkOrder order) implements DomainEvent {}

    record DeviceAssigned(String workOrderId, String deviceId) implements DomainEvent {}

    record LocationPredefined(String workOrderId, String deviceId, Location location) implements DomainEvent {}

    record BootNotificationConfirmed(String workOrderId, String deviceId,
                                     BootNotification boot, boolean confirmed) implements DomainEvent {}

    record InstallationCompleted(String workOrderId, String deviceId) implements DomainEvent {}
}
