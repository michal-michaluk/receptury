package devices.configuration.installations;

interface InstallationRepository {
    InstallationProcess getByOrderId(String deviceId);

    InstallationProcess getByDeviceId(String deviceId);

    void save(InstallationProcess process);
}
