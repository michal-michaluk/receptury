package devices.configuration.search;

import devices.configuration.device.DeviceConfiguration;
import devices.configuration.device.Location;
import devices.configuration.protocols.DeviceStatuses;

import java.util.List;
import java.util.Optional;

record DeviceSummary(
        String deviceId,
        Location location,
        List<String> statuses) {

    static DeviceSummary ofNullable(DeviceConfiguration details, DeviceStatuses statuses) {
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
