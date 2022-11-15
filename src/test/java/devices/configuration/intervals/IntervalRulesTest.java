package devices.configuration.intervals;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IntervalRulesTest {

    IntervalRules rules = IntervalRulesFixture.currentRules();

    @Test
    void matchInFirstDeviceIdRule() {
        DeviceInfo device = IntervalRulesFixture.matchingDeviceIdRule1();

        int interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).isEqualTo(600);
    }

    @Test
    void matchInSecondDeviceIdRule() {
        DeviceInfo device = IntervalRulesFixture.matchingDeviceIdRule2();

        int interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).isEqualTo(2700);
    }

    @Test
    void matchInStrictModelRule() {
        DeviceInfo device = IntervalRulesFixture.matchingStrictModelRule();

        int interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).isEqualTo(60);
    }

    @Test
    void matchInRegexpModelRule() {
        DeviceInfo device = IntervalRulesFixture.matchingRegexModelRule();

        int interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).isEqualTo(120);
    }

    @Test
    void matchInProtocolRule() {
        DeviceInfo device = IntervalRulesFixture.matchingProtocol20Rule();

        int interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).isEqualTo(600);
    }

    @Test
    void returnDefaultInterval() {
        DeviceInfo device = IntervalRulesFixture.notMatchingAnyRule();

        int interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).isEqualTo(1800);
    }
}
