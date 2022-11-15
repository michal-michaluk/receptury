package devices.configuration.intervals;

import devices.configuration.IntegrationTest;
import devices.configuration.JsonAssert;
import devices.configuration.configs.FeaturesConfigurationEntity;
import devices.configuration.configs.FeaturesConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static devices.configuration.TestTransaction.transactional;

@IntegrationTest
@Transactional
class IntervalRulesRepositoryTest {

    @Autowired
    private FeaturesConfigurationRepository repository;

    @Autowired
    private IntervalRulesDocumentRepository subject;

    @Test
    void shouldSaveAndLoadIntervalRules() {
        //given
        FeaturesConfigurationEntity entity = new FeaturesConfigurationEntity("IntervalRules", IntervalRulesFixture.currentRules());

        // when
        transactional(() -> repository.save(entity));
        var result = transactional(() -> subject.get());

        // then
        JsonAssert.assertThat(result).hasFieldsLike(IntervalRulesFixture.currentRules());
    }
}
