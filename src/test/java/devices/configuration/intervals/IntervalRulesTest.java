package devices.configuration.intervals;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class IntervalRulesTest {
    final IntervalRules rules = IntervalRulesFixture.intervalRules();

    @Test
    void matchRuleByDeviceIdCase1() {
        DeviceInfo device = IntervalRulesFixture.exampleDevice()
                .deviceId("EVB-P4562137")
                .build();
        Duration interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).hasSeconds(600);
    }

    @Test
    void matchRuleByDeviceIdCase2() {
        DeviceInfo device = IntervalRulesFixture.exampleDevice()
                .deviceId("t53_8264_019")
                .build();
        Duration interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).hasSeconds(2700);
    }

    @Test
    void matchRuleByModelExact() {
        DeviceInfo device = IntervalRulesFixture.exampleDevice()
                .vendor("ChargeStorm AB")
                .model("Chargestorm Connected")
                .build();
        Duration interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).hasSeconds(120);
    }

    @Test
    void matchRuleByModelRegexp() {
        DeviceInfo device = IntervalRulesFixture.exampleDevice()
                .vendor("Alfen BV")
                .model("NG920-52507")
                .build();
        Duration interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).hasSeconds(60);
    }

    @Test
    void matchRuleByFirmwareRegexp() {
        DeviceInfo device = IntervalRulesFixture.exampleDevice()
                .vendor("Alfen BV")
                .model("NG920-52507")
                .firmware("1.1.666")
                .build();
        Duration interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).hasSeconds(10);
    }

    @Test
    void notMatchAnyRule() {
        DeviceInfo device = IntervalRulesFixture.exampleDevice()
                .build();
        Duration interval = rules.calculateInterval(device);

        Assertions.assertThat(interval).hasSeconds(1800);
    }
}
