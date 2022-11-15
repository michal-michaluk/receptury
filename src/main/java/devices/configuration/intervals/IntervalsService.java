package devices.configuration.intervals;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class IntervalsService {
    private final IntervalRulesRepository repository;

    public int calculateInterval(DeviceInfo device) {
        IntervalRules rules = repository.get();
        return rules.calculateInterval(device);
    }
}
