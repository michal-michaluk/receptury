package devices.configuration.intervals;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class IntervalsService {

    private final IntervalsRepository repository;

    public Duration calculate(DeviceInfo device) {
        IntervalRules rules = repository.get();
        return rules.calculateInterval(device);
    }
}
