package devices.configuration.intervals;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IntervalRulesTest {

    IntervalRules rules = IntervalRulesFixture.currentRules();

    @Test
    void matchInFirstDeviceIdRule() {
        var boot = IntervalRulesFixture.matchingDeviceIdRule1();

        int interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).isEqualTo(600);
    }

    @Test
    void matchInSecondDeviceIdRule() {
        var boot = IntervalRulesFixture.matchingDeviceIdRule2();

        int interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).isEqualTo(2700);
    }

    @Test
    void matchInStrictModelRule() {
        var boot = IntervalRulesFixture.matchingStrictModelRule();

        int interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).isEqualTo(60);
    }

    @Test
    void matchInRegexpFirmwareRule() {
        var boot = IntervalRulesFixture.matchingRegexFirmwareRule();

        int interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).isEqualTo(10);
    }

    @Test
    void matchInRegexpModelRule() {
        var boot = IntervalRulesFixture.matchingRegexModelRule();

        int interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).isEqualTo(120);
    }

    @Test
    void returnDefaultInterval() {
        var boot = IntervalRulesFixture.notMatchingAnyRule();

        int interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).isEqualTo(1800);
    }
}
