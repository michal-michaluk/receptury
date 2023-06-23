package devices.configuration.mediators;

import devices.configuration.intervals.IntervalsService;
import devices.configuration.protocols.BootNotification;
import devices.configuration.protocols.HeartbeatInterval;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@AllArgsConstructor
public class ProtocolsToIntervalsMediator implements HeartbeatInterval {

    private final IntervalsService intervals;

    @Override
    public Duration heartbeatIntervalFor(BootNotification boot) {
        return intervals.calculateInterval(boot);
    }
}
