package devices.configuration.protocols;


import devices.configuration.intervals.IntervalsService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

@Service
@Transactional
@AllArgsConstructor
public class CommunicationService {
    private final Clock clock;
    private final IntervalsService intervals;
    private final KnownDevices devices;
    private final ApplicationEventPublisher publisher;

    public BootResponse handleBoot(BootNotification boot) {
        BootResponse response = new BootResponse(
                Instant.now(clock),
                Duration.ofSeconds(intervals.calculateInterval(boot)),
                devices.get(boot.deviceId())
        );
        publisher.publishEvent(boot);
        return response;
    }

    public record BootResponse(Instant serverTime, Duration interval, KnownDevices.State state) {

        public <T> T map(Function<BootResponse, T> func) {
            return func.apply(this);
        }

        public int intervalInSeconds() {
            return (int) interval.getSeconds();
        }

        public <T> T state(Function<KnownDevices.State, T> func) {
            return func.apply(state);
        }
    }
}
