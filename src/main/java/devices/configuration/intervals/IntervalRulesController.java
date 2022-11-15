package devices.configuration.intervals;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
class IntervalRulesController {

    private final IntervalRulesDocumentRepository intervalRules;

    @GetMapping(path = "/configs/IntervalRules",
            produces = MediaType.APPLICATION_JSON_VALUE)
    IntervalRules get() {
        return intervalRules.get();
    }

    @PutMapping(path = "/configs/IntervalRules",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void put(@RequestBody @Valid IntervalRules configuration) {
        intervalRules.save(configuration);
    }
}
