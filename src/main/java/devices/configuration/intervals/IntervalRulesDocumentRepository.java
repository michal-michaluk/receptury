package devices.configuration.intervals;

import com.fasterxml.jackson.databind.JsonNode;
import devices.configuration.tools.FeatureConfiguration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class IntervalRulesDocumentRepository implements IntervalRulesRepository {

    public static final String CONFIG_NAME = "IntervalRules";
    private final FeatureConfiguration repository;

    @Override
    public IntervalRules get() {
        return repository.get(CONFIG_NAME)
                .as(IntervalRules.class)
                .orElse(IntervalRules.defaultRules());
    }

    public JsonNode save(IntervalRules configuration) {
        return repository.save(CONFIG_NAME, configuration)
                .raw();
    }
}
