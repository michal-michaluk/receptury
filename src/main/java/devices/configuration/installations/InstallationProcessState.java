package devices.configuration.installations;

public record InstallationProcessState(String orderId, String deviceId, State state) {
    enum State {PENDING, DEVICE_ASSIGNED, BOOTED, FINALIZED}
}
