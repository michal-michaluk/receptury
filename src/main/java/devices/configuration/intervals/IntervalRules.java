package devices.configuration.intervals;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class IntervalRules {

    private final List<DeviceIdRule> deviceIdRules;
    private final List<ModelRule> modelRules;
    private final int defaultInterval;

    IntervalRules(List<DeviceIdRule> deviceIdRules, List<ModelRule> modelRules, int defaultInterval) {
        this.deviceIdRules = deviceIdRules;
        this.modelRules = modelRules;
        this.defaultInterval = defaultInterval;
    }

    Duration calculateInterval(DeviceInfo device) {
        return Stream.of(deviceIdRules, modelRules)
                .flatMap(List::stream)
                .filter(rule -> rule.matches(device))
                .findFirst()
                .map(rule -> Duration.ofSeconds(rule.interval()))
                .orElse(Duration.ofSeconds(defaultInterval));
    }

    interface Rule {
        boolean matches(DeviceInfo device);

        int interval();
    }

    record DeviceIdRule(int interval, List<String> deviceIds) implements Rule {
        @Override
        public boolean matches(DeviceInfo device) {
            return deviceIds.contains(device.deviceId());
        }
    }

    record ModelRule(int interval, String vendor, Pattern model, Pattern firmware) implements Rule {
        ModelRule(int interval, String vendor, Pattern model) {
            this(interval, vendor, model, null);
        }

        @Override
        public boolean matches(DeviceInfo device) {
            return vendor.equals(device.vendor())
                   && model.matcher(device.model()).matches()
                   && (firmware == null || firmware.matcher(device.firmware()).matches());
        }
    }

}
