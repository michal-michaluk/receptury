package devices.configuration.installations;

import devices.configuration.device.DeviceService;
import devices.configuration.device.Location;
import devices.configuration.device.UpdateDevice;
import devices.configuration.protocols.BootNotification;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InstallationService {

    private final InstallationRepository repository;
    private final DeviceService devices;

    void initInstallation(WorkOrder order) {
        InstallationProcess process = InstallationProcess.startInstallationProcessFor(order);
        repository.save(process);
    }

    void assignDevice(String workOrderId, String deviceId) {
        InstallationProcess process = repository.getByOrderId(workOrderId);
        process.assignDevice(deviceId);
        repository.save(process);
    }

    void assignLocation(String deviceId, Location location) {
        InstallationProcess process = repository.getByDeviceId(deviceId);
        process.assignLocation(location);
        repository.save(process);
    }

    @EventListener
    public void handleBootNotification(BootNotification boot) {
        InstallationProcess process = repository.getByDeviceId(boot.deviceId());
        process.handleBootNotification(boot);
        repository.save(process);
    }

    void confirmBootData(String deviceId) {
        InstallationProcess process = repository.getByDeviceId(deviceId);
        process.confirmBootData();
        repository.save(process);
    }

    CompletionResult complete(String deviceId) {
        InstallationProcess process = repository.getByDeviceId(deviceId);
        CompletionResult finalization = process.complete();
        if (finalization.isConfirmed()) {
            devices.createNewDevice(deviceId, UpdateDevice.update(
                    finalization.ownership(), finalization.location())
            );
        }
        return finalization;
    }
}
