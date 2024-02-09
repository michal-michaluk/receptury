package devices.configuration.intervals;

import devices.configuration.communication.BootNotification;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class IntervalsService {
    private final IntervalRulesRepository repository;

    @WithSpan
    public Duration calculateInterval(BootNotification boot) {
        IntervalRules rules = repository.get();
        return rules.calculateInterval(boot);
    }
}
