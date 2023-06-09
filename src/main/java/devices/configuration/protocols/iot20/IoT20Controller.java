package devices.configuration.protocols.iot20;

import devices.configuration.intervals.IntervalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
class IoT20Controller {

    private final Clock clock;
    private final IntervalsService intervals;
    private final ApplicationEventPublisher publisher;

    @PostMapping(path = "/protocols/iot20/bootnotification/{deviceId}",
            consumes = "application/json", produces = "application/json")
    BootNotificationResponse handleBootNotification(@PathVariable String deviceId,
                                                    @RequestBody BootNotificationRequest request) {
        publisher.publishEvent(request.toBootNotificationEvent(deviceId));

        return new BootNotificationResponse(
                Instant.now(clock).toString(),
                intervals.calculateInterval(request.toDevice(deviceId)),
                BootNotificationResponse.Status.Accepted);
    }
}
