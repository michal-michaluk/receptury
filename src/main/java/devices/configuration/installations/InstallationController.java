package devices.configuration.installations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import devices.configuration.device.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
class InstallationController {

    private final InstallationService service;
    private final InstallationReadModel reads;

    @GetMapping(path = "/installations", params = {"page", "size"},
            produces = APPLICATION_JSON_VALUE)
    Page<InstallationProcessState> getPage(
            InstallationReadModel.QueryParams params,
            Pageable pageable) {
        return reads.query(params, pageable);
    }

    @GetMapping(path = "/installations/{orderId}",
            produces = APPLICATION_JSON_VALUE)
    InstallationProcessState get(@PathVariable String orderId) {
        return reads.queryByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order not found"));
    }

    @PatchMapping(path = "/installations/{orderId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    InstallationProcessState patch(@PathVariable String orderId,
                                   @RequestBody @Valid Command body) {
        switch (body) {
            case AssignDeviceId command -> service.assignDevice(orderId, command.assignDevice);
            case AssignLocation command -> service.assignLocation(orderId, command.assignLocation);
            case ConfirmBoot ignore -> service.confirmBootData(orderId);
            case CompleteInstallation ignore -> service.complete(orderId);
        }
        return service.getByOrderId(orderId);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
            @Type(AssignDeviceId.class),
            @Type(AssignLocation.class),
            @Type(ConfirmBoot.class),
            @Type(CompleteInstallation.class)
    })
    sealed interface Command {}

    record AssignDeviceId(@NotBlank String assignDevice) implements Command {}

    record AssignLocation(@NotNull @Valid Location assignLocation) implements Command {}

    record ConfirmBoot(@AssertTrue boolean confirmBoot) implements Command {}

    record CompleteInstallation(@AssertTrue boolean complete) implements Command {}
}
