package devices.configuration.intervals;

import lombok.Builder;

@Builder(toBuilder = true)
public record DeviceInfo(String deviceId, String vendor, String model, String firmware) {
}

