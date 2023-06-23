package devices.configuration.protocols;

import java.time.Duration;

public interface HeartbeatInterval {
    Duration heartbeatIntervalFor(BootNotification boot);
}
