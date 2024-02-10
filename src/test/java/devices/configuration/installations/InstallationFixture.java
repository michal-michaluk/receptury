package devices.configuration.installations;

import devices.configuration.device.Ownership;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InstallationFixture {
    @NotNull
    static WorkOrder givenWorkOrderFor(@NotNull Ownership ownership) {
        return new WorkOrder(UUID.randomUUID().toString(), ownership);
    }

    @NotNull
    static InstallationProcessState state(String orderId, String deviceId, InstallationProcessState.State state) {
        return new InstallationProcessState(orderId, deviceId, state);
    }
}
