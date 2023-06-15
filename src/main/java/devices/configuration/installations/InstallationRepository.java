package devices.configuration.installations;

import java.util.Optional;

interface InstallationRepository {
    InstallationProcess getByOrderId(String deviceId);

    Optional<InstallationProcess> getByDeviceId(String deviceId);

    void save(InstallationProcess process);
}
