package devices.configuration.protocols;

import lombok.Builder;

@Builder(toBuilder = true)
public record BootNotification(
        String deviceId,
        String vendor, String model,
        String serial, String firmware) {
}
