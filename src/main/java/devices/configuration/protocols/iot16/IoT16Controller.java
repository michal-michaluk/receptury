package devices.configuration.protocols.iot16;

import devices.configuration.protocols.CommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static devices.configuration.protocols.iot16.BootNotificationResponse.Status.*;

@RestController
@RequiredArgsConstructor
class IoT16Controller {

    private final CommunicationService service;

    @PostMapping(path = "/protocols/iot16/bootnotification/{deviceId}",
            consumes = "application/json", produces = "application/json")
    BootNotificationResponse handleBootNotification(@PathVariable String deviceId,
                                                    @RequestBody BootNotificationRequest request) {
        return service.handleBoot(request.toBootNotificationEvent(deviceId))
                .map(resp -> BootNotificationResponse.builder()
                        .currentTime(resp.serverTime().toString())
                        .interval(resp.intervalInSeconds())
                        .status(resp.state(state -> switch (state) {
                                    case UNKNOWN -> Rejected;
                                    case IN_INSTALLATION -> Pending;
                                    case EXISTING -> Accepted;
                                })
                        ).build()
                );
    }

}
