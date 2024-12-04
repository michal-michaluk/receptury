package devices.configuration.search;

import devices.configuration.auth.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
class DeviceReadsController {

    private final DevicesReadModel reads;

    @GetMapping(path = "/devices", params = {"page", "size"},
            produces = "application/vnd.device.summary+json")
    Page<DeviceSummary> getSummary(String operator, Pageable pageable, Identity identity) {
        if (!identity.operators().contains(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return reads.querySummary(operator, pageable);
    }

    @GetMapping(path = "/devices",
            produces = "application/vnd.device.pin+json")
    List<DevicePin> getPins(String operator, Identity identity) {
        if (!identity.operators().contains(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return reads.queryPins(operator);
    }

    @GetMapping(path = "/devices/{deviceId}",
            produces = APPLICATION_JSON_VALUE)
    DeviceDetails getDetails(@PathVariable String deviceId, Identity identity) {
        return reads.queryDetails(deviceId)
                .filter(device -> device.configuration() != null)
                .filter(device -> identity.matches(device.configuration().ownership()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }
}
