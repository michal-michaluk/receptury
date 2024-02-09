package devices.configuration.mediators;

import devices.configuration.device.DeviceService;
import devices.configuration.device.Location;
import devices.configuration.device.Ownership;
import devices.configuration.device.UpdateDevice;
import devices.configuration.installations.Devices;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InstallationsToDevicesMediator implements Devices {

    private final DeviceService devices;

    @Override
    @WithSpan
    public void create(String deviceId, Ownership ownership, Location location) {
        devices.createNewDevice(
                deviceId,
                UpdateDevice.use(ownership, location)
        );
    }
}
