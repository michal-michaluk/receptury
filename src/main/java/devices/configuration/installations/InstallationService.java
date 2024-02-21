package devices.configuration.installations;

import devices.configuration.communication.BootNotification;
import devices.configuration.device.Location;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class InstallationService {

    private final InstallationRepository repository;
    private final Devices devices;

    @EventListener
    public void handleWorkOrder(WorkOrder order) {
        InstallationProcess process = InstallationProcess.startInstallationProcessFor(order);
        repository.save(process);
    }

    void assignDevice(String orderId, String deviceId) {
        InstallationProcess process = repository.getByOrderId(orderId);
        process.assignDevice(deviceId);
        repository.save(process);
    }

    void assignLocation(String orderId, Location location) {
        InstallationProcess process = repository.getByOrderId(orderId);
        process.assignLocation(location);
        repository.save(process);
    }

    @EventListener
    public void handleBootNotification(BootNotification boot) {
        repository.getByDeviceId(boot.deviceId())
                .ifPresent(process -> {
                    process.handleBootNotification(boot);
                    repository.save(process);
                });
    }

    void confirmBootData(String orderId) {
        InstallationProcess process = repository.getByOrderId(orderId);
        process.confirmBootData();
        repository.save(process);
    }

    CompletionResult complete(String orderId) {
        InstallationProcess process = repository.getByOrderId(orderId);
        CompletionResult result = process.complete();
        repository.save(process);
        if (result.isConfirmed()) {
            devices.create(
                    process.deviceId,
                    result.ownership(),
                    result.location()
            );
        }
        return result;
    }

    public Optional<InstallationProcessState> getByDeviceId(String deviceId) {
        return repository.getByDeviceId(deviceId)
                .map(InstallationProcess::asState);
    }

    public InstallationProcessState getByOrderId(String orderId) {
        return repository.getByOrderId(orderId).asState();
    }
}
