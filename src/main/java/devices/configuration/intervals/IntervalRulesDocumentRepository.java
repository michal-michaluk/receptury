package devices.configuration.intervals;

import com.fasterxml.jackson.databind.JsonNode;
import devices.configuration.tools.FeaturesConfigurationEntity;
import devices.configuration.tools.FeaturesConfigurationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class IntervalRulesDocumentRepository implements IntervalRulesRepository {

    public static final String CONFIG_NAME = "IntervalRules";
    private final FeaturesConfigurationRepository repository;

    @Override
    public IntervalRules get() {
        return repository.findByName(CONFIG_NAME)
                .map(e -> e.configAs(IntervalRules.class))
                .orElse(IntervalRules.defaultRules());
    }

    public JsonNode save(IntervalRules configuration) {
        return repository.findByName(CONFIG_NAME)
                .orElseGet(() -> repository.save(new FeaturesConfigurationEntity(CONFIG_NAME)))
                .withConfiguration(configuration)
                .getConfiguration();
    }
}
