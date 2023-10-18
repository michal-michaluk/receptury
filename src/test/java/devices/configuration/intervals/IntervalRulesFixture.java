package devices.configuration.intervals;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class IntervalRulesFixture {

    @NotNull
    public static IntervalRules intervalRules() {
        return new IntervalRules(
                List.of(
                        new IntervalRules.DeviceIdRule(600, List.of("EVB-P4562137", "ALF-9571445")),
                        new IntervalRules.DeviceIdRule(2700, List.of("t53_8264_019", "EVB-P15079256"))
                ),
                List.of(
                        new IntervalRules.ModelRule(120, "ChargeStorm AB", Pattern.compile("Chargestorm Connected")),
                        new IntervalRules.ModelRule(60, "Alfen BV", Pattern.compile("NG920-5250[6-9]"))
                ),
                1800);
    }

    @NotNull
    public static DeviceInfo.DeviceInfoBuilder exampleDevice() {
        return new DeviceInfo(
                "CH-0000000",
                "ChargeStorm ABC",
                "Chargestorm NotConnected",
                "1.2.3"
        ).toBuilder();

    }
}
