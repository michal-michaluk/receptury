package devices.configuration.communication.protocols.iot20;

import devices.configuration.tools.JsonConfiguration;

public class Ocpp20MessagesFixture {

    public static BootNotificationRequest bootNotification() {
        return new BootNotificationRequest(
                new BootNotificationRequest.Device(
                        "820394A93203",
                        "CPF25 Family",
                        new BootNotificationRequest.Modem(
                                "1122 3344 5566 7788 99 C 1",
                                "082931213347973812"
                        ),
                        "EVB",
                        "1.0"
                ),
                BootNotificationRequest.Reason.PowerUp
        );
    }

    public static String json() {
        return JsonConfiguration.json(bootNotification());
    }
}
