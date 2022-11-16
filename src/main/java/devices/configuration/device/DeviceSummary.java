package devices.configuration.device;

import devices.configuration.device.DomainEvent.DeviceStatuses;

import java.util.List;
import java.util.Optional;

record DeviceSummary(
        String deviceId,
        Location location,
        List<String> statuses) {

    static DeviceSummary ofNullable(DeviceSnapshot details, DeviceStatuses statuses) {
        if (details == null || details.location() == null) {
            return null;
        }
        return new DeviceSummary(
                details.deviceId(),
                details.location(),
                Optional.ofNullable(statuses)
                        .map(DeviceStatuses::statuses)
                        .orElse(List.of("Faulted")));
    }
}
