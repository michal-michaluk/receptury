package devices.configuration.mediators;

import devices.configuration.device.DeviceService;
import devices.configuration.installations.InstallationService;
import devices.configuration.protocols.KnownDevices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
class ProtocolsToDevicesAndInstallationsMediator implements KnownDevices {
    private final DeviceService devices;
    private final InstallationService installations;

    @Override
    public State get(String deviceId) {
        if (devices.get(deviceId).isPresent()) {
            return State.EXISTING;
        }
        if (installations.getByDeviceId(deviceId).isPresent()) {
            return State.IN_INSTALLATION;
        }
        return State.UNKNOWN;
    }
}
