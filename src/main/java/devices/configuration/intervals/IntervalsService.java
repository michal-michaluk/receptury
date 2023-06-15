package devices.configuration.intervals;

import devices.configuration.protocols.BootNotification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class IntervalsService {
    private final IntervalRulesRepository repository;

    public int calculateInterval(BootNotification boot) {
        IntervalRules rules = repository.get();
        return rules.calculateInterval(boot);
    }
}
