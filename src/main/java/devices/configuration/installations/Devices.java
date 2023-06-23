package devices.configuration.installations;

import devices.configuration.device.Location;
import devices.configuration.device.Ownership;

public interface Devices {
    void create(String deviceId, Ownership ownership, Location location);
}
