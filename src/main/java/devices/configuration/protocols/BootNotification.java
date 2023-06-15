package devices.configuration.protocols;

import lombok.Builder;

@Builder(toBuilder = true)
public record BootNotification(
        String deviceId, Protocols protocol,
        String vendor, String model,
        String serial, String firmware) {

    public enum Protocols {
        IoT20, IoT16
    }
}
