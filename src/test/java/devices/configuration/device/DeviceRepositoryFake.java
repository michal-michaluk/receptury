package devices.configuration.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class DeviceRepositoryFake implements DeviceRepository {
    final Map<String, Device> inMemory = new HashMap<>();

    @Override
    public Optional<Device> get(String deviceId) {
        return Optional.ofNullable(inMemory.get(deviceId));
    }

    @Override
    public void save(Device device) {
        inMemory.put(device.deviceId, device);
    }
}
