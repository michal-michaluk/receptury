package devices.configuration.intervals;

import devices.configuration.IntegrationTest;
import devices.configuration.tools.FeatureConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static devices.configuration.JsonAssert.assertThat;
import static devices.configuration.TestTransaction.transactional;

@IntegrationTest
@Transactional
class IntervalRulesRepositoryTest {

    @Autowired
    private FeatureConfiguration repository;

    @Autowired
    private IntervalRulesDocumentRepository subject;

    @Test
    void shouldSaveAndLoadIntervalRules() {
        // when
        transactional(() -> repository.save("IntervalRules", IntervalRulesFixture.currentRules()));
        var result = transactional(() -> subject.get());

        // then
        assertThat(result).hasFieldsLike(IntervalRulesFixture.currentRules());
        assertThat(result).isExactlyLike("""
                {
                  "byIds": [
                    {
                      "seconds": 600,
                      "devices": [
                        "ALF-2844179",
                        "ALF-9571445",
                        "CS_7155_CGC100",
                        "EVB-P4562137",
                        "EVB-P9287312"
                      ]
                    },
                    {
                      "seconds": 2700,
                      "devices": [
                        "EVB-P0984003",
                        "EVB-P15079256",
                        "EVB-P1515526",
                        "EVB-P1515640",
                        "t53_8264_019"
                      ]
                    }
                  ],
                  "byModel": [
                    {
                      "seconds": 60,
                      "vendor": "Alfen BV",
                      "model": "NG920-5250[6-9]",
                      "firmware": null
                    },
                    {
                      "seconds": 10,
                      "vendor": "ChargeStorm AB",
                      "model": "Chargestorm Connected",
                      "firmware": "1[.]2[.].*"
                    },
                    {
                      "seconds": 60,
                      "vendor": "ChargeStorm AB",
                      "model": "Chargestorm Connected",
                      "firmware": null
                    },
                    {
                      "seconds": 120,
                      "vendor": "EV-BOX",
                      "model": "G3-M5320E-F2.*",
                      "firmware": null
                    }
                  ],
                  "defSeconds": 1800
                }
                """);
    }
}
