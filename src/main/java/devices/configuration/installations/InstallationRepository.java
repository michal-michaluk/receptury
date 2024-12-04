package devices.configuration.installations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

interface InstallationRepository {
    InstallationProcess getByOrderId(String deviceId);

    Optional<InstallationProcess> getByDeviceId(String deviceId);

    void save(InstallationProcess process);

    Page<InstallationProcessState> findAllMatching(boolean anyStatus, List<InstallationProcessState.State> states, Pageable pageable);
}
