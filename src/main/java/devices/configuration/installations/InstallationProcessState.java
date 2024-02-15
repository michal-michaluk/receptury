package devices.configuration.installations;

public record InstallationProcessState(String orderId, String deviceId, State state) {
    public enum State {PENDING, DEVICE_ASSIGNED, BOOTED, COMPLETED}
}
