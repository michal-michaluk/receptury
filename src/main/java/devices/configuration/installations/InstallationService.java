package devices.configuration.installations;

import devices.configuration.communication.BootNotification;
import devices.configuration.device.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class InstallationService {

    private final InstallationRepository repository;

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
        if (result.isConfirmed()) {
            // integrate with devices -> create new device
        }
        return result;
    }

    public Optional<InstallationProcessState> getByDeviceId(String deviceId) {
        return repository.getByDeviceId(deviceId)
                .map(InstallationProcess::asState);
    }

    Optional<InstallationProcessState> getByOrderId(String deviceId) {
        return repository.getByDeviceId(deviceId)
                .map(InstallationProcess::asState);
    }

    Page<InstallationProcessState> query(QueryParams params, Pageable pageable) {
        return repository.findAllMatching(params.anyStatus(), params.states(), pageable);
    }


    @Builder(builderMethodName = "params")
    record QueryParams(List<InstallationProcessState.State> states) {
        QueryParams(List<InstallationProcessState.State> states) {
            this.states = states == null ? List.of() : states;
        }

        public boolean anyStatus() {
            return states.isEmpty();
        }
    }

}
