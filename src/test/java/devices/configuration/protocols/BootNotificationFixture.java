package devices.configuration.protocols;

import org.jetbrains.annotations.NotNull;

public class BootNotificationFixture {

    @NotNull
    public static BootNotification.BootNotificationBuilder boot() {
        return BootNotification.builder()
                .vendor("Garo")
                .model("CPF25 Family")
                .serial("820394A93203")
                .firmware("1.1");
    }

    @NotNull
    public static BootNotification boot(String deviceId) {
        return boot().deviceId(deviceId).build();
    }
}
