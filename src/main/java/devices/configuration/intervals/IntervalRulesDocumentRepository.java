package devices.configuration.intervals;

import devices.configuration.configs.FeaturesConfigurationEntity;
import devices.configuration.configs.FeaturesConfigurationRepository;
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
                .map(FeaturesConfigurationEntity::getConfiguration)
                .orElse(IntervalRules.defaultRules());
    }

    public IntervalRules save(IntervalRules configuration) {
        return repository.findByName(CONFIG_NAME)
                .orElseGet(() -> repository.save(new FeaturesConfigurationEntity(CONFIG_NAME, configuration)))
                .withConfiguration(configuration)
                .getConfiguration();
    }
}
