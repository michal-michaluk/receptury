package devices.configuration.device;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
class DevicesController {

    private final DeviceReads reads;
    private final DeviceService service;

    @Transactional(readOnly = true)
    @GetMapping(path = "/devices", params = {"page", "size"},
            produces = "application/vnd.device.summary+json")
    Page<DeviceSummary> getSummary(String provider, Pageable pageable) {
        return reads.findAllSummary(provider, pageable);
    }

    @Transactional(readOnly = true)
    @GetMapping(path = "/devices", params = {"page", "size"},
            produces = "application/vnd.device.pin+json")
    List<DevicePin> getPins(String provider) {
        return reads.findAllPins(provider);
    }

    @Transactional(readOnly = true)
    @GetMapping(path = "/devices/{deviceId}",
            produces = APPLICATION_JSON_VALUE)
    Optional<DeviceSnapshot> get(@PathVariable String deviceId) {
        return reads.findById(deviceId);
    }

    @PatchMapping(path = "/devices/{deviceId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    DeviceSnapshot patchStation(@PathVariable String deviceId,
                                @RequestBody @Valid UpdateDevice update) {
        return service.update(deviceId, update)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
