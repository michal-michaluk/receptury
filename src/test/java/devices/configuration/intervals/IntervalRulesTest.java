package devices.configuration.intervals;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IntervalRulesTest {

    IntervalRules rules = IntervalRulesFixture.currentRules();

    @Test
    void matchInFirstDeviceIdRule() {
        var boot = IntervalRulesFixture.matchingDeviceIdRule1();

        var interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).hasSeconds(600);
    }

    @Test
    void matchInSecondDeviceIdRule() {
        var boot = IntervalRulesFixture.matchingDeviceIdRule2();

        var interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).hasSeconds(2700);
    }

    @Test
    void matchInStrictModelRule() {
        var boot = IntervalRulesFixture.matchingStrictModelRule();

        var interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).hasSeconds(60);
    }

    @Test
    void matchInRegexpFirmwareRule() {
        var boot = IntervalRulesFixture.matchingRegexFirmwareRule();

        var interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).hasSeconds(10);
    }

    @Test
    void matchInRegexpModelRule() {
        var boot = IntervalRulesFixture.matchingRegexModelRule();

        var interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).hasSeconds(120);
    }

    @Test
    void returnDefaultInterval() {
        var boot = IntervalRulesFixture.notMatchingAnyRule();

        var interval = rules.calculateInterval(boot);

        Assertions.assertThat(interval).hasSeconds(1800);
    }
}
