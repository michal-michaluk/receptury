package devices.configuration.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import devices.configuration.communication.BootNotification;
import devices.configuration.device.DeviceConfiguration;

record DeviceDetails(
        @JsonUnwrapped
        DeviceConfiguration configuration,
        @JsonIgnoreProperties({"deviceId"})
        BootNotification boot) {
}
