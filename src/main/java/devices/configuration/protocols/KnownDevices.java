package devices.configuration.protocols;

public interface KnownDevices {
    enum State {UNKNOWN, IN_INSTALLATION, EXISTING}

    State get(String deviceId);
}
