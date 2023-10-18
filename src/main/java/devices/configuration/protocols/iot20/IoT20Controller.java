package devices.configuration.protocols.iot20;

import devices.configuration.intervals.IntervalsService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping(path = "/protocols/iot20/bootnotification/{deviceId}",
            consumes = "application/json", produces = "application/json")
    BootNotificationResponse handleBootNotification(@PathVariable String deviceId,
                                                    @RequestBody BootNotificationRequest request) {
        return new BootNotificationResponse(
                Instant.now(clock).toString(),
                (int) intervals.calculate(request.toDeviceInfo(deviceId)).getSeconds(),
                BootNotificationResponse.Status.Accepted);
    }
}
