package devices.configuration.search;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import devices.configuration.device.DeviceConfiguration;
import devices.configuration.protocols.BootNotification;

record DeviceDetails(
        @JsonUnwrapped
        DeviceConfiguration configuration,
        BootNotification boot) {
}
