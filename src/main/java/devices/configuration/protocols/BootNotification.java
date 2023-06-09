package devices.configuration.protocols;

public record BootNotification(
        String deviceId,
        String vendor, String model,
        String serial, String firmware) {
}
