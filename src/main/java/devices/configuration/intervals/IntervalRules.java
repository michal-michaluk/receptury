package devices.configuration.intervals;

import devices.configuration.communication.BootNotification;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

record IntervalRules(
        List<DeviceIdRule> byIds,
        List<ModelRule> byModel,
        int defSeconds) {

    static IntervalRules defaultRules() {
        return new IntervalRules(List.of(), List.of(), 1800);
    }

    static DeviceIdRule deviceIdRule(int seconds, Set<String> deviceId) {
        return new DeviceIdRule(seconds, new TreeSet<>(deviceId));
    }

    static ModelRule modelRule(int seconds, String vendor, Pattern model) {
        return new ModelRule(seconds, vendor, model, null);
    }

    static ModelRule firmwareRule(int seconds, String vendor, Pattern model, Pattern firmware) {
        return new ModelRule(seconds, vendor, model, firmware);
    }

    Duration calculateInterval(BootNotification boot) {
        return Duration.ofSeconds(Stream.of(byIds, byModel)
                .flatMap(Collection::stream)
                .filter(rule -> rule.matches(boot))
                .findFirst()
                .map(Rule::seconds)
                .orElse(defSeconds));
    }

    interface Rule {
        boolean matches(BootNotification boot);

        int seconds();
    }

    record DeviceIdRule(int seconds, SortedSet<String> devices) implements Rule {
        @Override
        public boolean matches(BootNotification boot) {
            return devices.contains(boot.deviceId());
        }
    }

    record ModelRule(int seconds, String vendor, Pattern model, Pattern firmware) implements Rule {
        @Override
        public boolean matches(BootNotification boot) {
            return Objects.equals(vendor, boot.vendor())
                   && model.matcher(boot.model()).matches()
                   && (firmware == null || firmware.matcher(boot.firmware()).matches());
        }
    }
}
