package devices.configuration.installations;

import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static devices.configuration.installations.InstallationProcess.startInstallationProcessFor;

@AllArgsConstructor
public class ProcessFixture {

    public String orderId;
    public String deviceId;

    public static ProcessFixture given() {
        return new ProcessFixture(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public static ProcessFixture given(String orderId, String deviceId) {
        return new ProcessFixture(orderId, deviceId);
    }

    @NotNull
    public InstallationProcess newProcess() {
        return startInstallationProcessFor(new WorkOrder(orderId, DeviceFixture.ownership()));
    }

    @NotNull
    public InstallationProcess withDeviceAssigned() {
        InstallationProcess process = newProcess();
        process.assignDevice(deviceId);
        return process;
    }

    @NotNull
    public InstallationProcess almostCompleted() {
        InstallationProcess process = withDeviceAssigned();
        process.handleBootNotification(CommunicationFixture.boot(deviceId));
        process.confirmBootData();
        process.assignLocation(DeviceFixture.location());
        return process;
    }

    public InstallationProcess completed() {
        var process = almostCompleted();
        process.complete();
        return process;
    }
}
