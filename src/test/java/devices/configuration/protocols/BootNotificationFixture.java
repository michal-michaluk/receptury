package devices.configuration.protocols;

import org.jetbrains.annotations.NotNull;

public class BootNotificationFixture {

    @NotNull
    public static BootNotification boot(String deviceId) {
        return new BootNotification(
                deviceId,
                "Garo",
                "CPF25 Family",
                "820394A93203",
                "1.1"
        );
    }
}
