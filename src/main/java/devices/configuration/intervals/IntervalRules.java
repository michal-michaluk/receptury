package devices.configuration.intervals;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

record IntervalRules(
        List<DeviceIdRule> byIds,
        List<ModelRule> byModel,
        List<ProtocolRule> byProtocol,
        int defSeconds) {

    public static IntervalRules defaultRules() {
        return new IntervalRules(List.of(), List.of(), List.of(), 1800);
    }

    static DeviceIdRule byDeviceIdRule(int seconds, Set<String> deviceId) {
        return new DeviceIdRule(seconds, new TreeSet<>(deviceId));
    }

    static ModelRule byModelRule(int seconds, String vendor, Pattern model) {
        return new ModelRule(seconds, vendor, model);
    }

    static ProtocolRule byProtocolRule(int seconds, Protocols protocol) {
        return new ProtocolRule(seconds, protocol);
    }

    int calculateInterval(DeviceInfo device) {
        return Stream.of(byIds, byModel, byProtocol)
                .flatMap(Collection::stream)
                .filter(rule -> rule.matches(device))
                .findFirst()
                .map(Rule::seconds)
                .orElse(defSeconds);
    }

    interface Rule {
        boolean matches(DeviceInfo device);

        int seconds();
    }

    record DeviceIdRule(int seconds, SortedSet<String> devices) implements Rule {
        @Override
        public boolean matches(DeviceInfo device) {
            return devices.contains(device.deviceId());
        }
    }

    record ModelRule(int seconds, String vendor, Pattern model) implements Rule {
        @Override
        public boolean matches(DeviceInfo device) {
            return Objects.equals(vendor, device.vendor())
                    && model.matcher(device.model()).matches();
        }
    }

    record ProtocolRule(int seconds, Protocols protocol) implements Rule {
        @Override
        public boolean matches(DeviceInfo device) {
            return Objects.equals(protocol, device.protocol());
        }
    }
}
