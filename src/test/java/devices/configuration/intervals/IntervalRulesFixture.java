package devices.configuration.intervals;

import devices.configuration.protocols.BootNotification;
import devices.configuration.protocols.CommunicationFixture;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static devices.configuration.intervals.IntervalRules.*;

public class IntervalRulesFixture {

    private static final String T_53_8264_019 = "t53_8264_019";
    private static final String EVB_P_4562137 = "EVB-P4562137";

    public static IntervalRules currentRules() {
        return new IntervalRules(
                List.of(
                        deviceIdRule(600, Set.of("EVB-P4562137", "ALF-9571445", "CS_7155_CGC100", "EVB-P9287312", "ALF-2844179")),
                        deviceIdRule(2700, Set.of("t53_8264_019", "EVB-P15079256", "EVB-P0984003", "EVB-P1515640", "EVB-P1515526"))),
                List.of(
                        modelRule(60, "Alfen BV", Pattern.compile("NG920-5250[6-9]")),
                        firmwareRule(10, "ChargeStorm AB", Pattern.compile("Chargestorm Connected"), Pattern.compile("1[.]2[.].*")),
                        modelRule(60, "ChargeStorm AB", Pattern.compile("Chargestorm Connected")),
                        modelRule(120, "EV-BOX", Pattern.compile("G3-M5320E-F2.*"))),
                1800
        );
    }

    @NotNull
    public static BootNotification.BootNotificationBuilder givenDevice() {
        return CommunicationFixture.boot()
                .deviceId("EVB-P4123437");
    }

    public static BootNotification notMatchingAnyRule() {
        return givenDevice().build();
    }

    public static BootNotification matchingDeviceIdRule1() {
        return givenDevice()
                .deviceId(EVB_P_4562137)
                .build();
    }

    public static BootNotification matchingDeviceIdRule2() {
        return givenDevice()
                .deviceId(T_53_8264_019)
                .build();
    }

    public static BootNotification matchingStrictModelRule() {
        return IntervalRulesFixture.givenDevice()
                .vendor("ChargeStorm AB")
                .model("Chargestorm Connected")
                .build();
    }

    public static BootNotification matchingRegexFirmwareRule() {
        return IntervalRulesFixture.givenDevice()
                .vendor("ChargeStorm AB")
                .model("Chargestorm Connected")
                .firmware("1.2.10")
                .build();
    }

    public static BootNotification matchingRegexModelRule() {
        return IntervalRulesFixture.givenDevice()
                .vendor("EV-BOX")
                .model("G3-M5320E-F2-5321")
                .build();
    }
}
