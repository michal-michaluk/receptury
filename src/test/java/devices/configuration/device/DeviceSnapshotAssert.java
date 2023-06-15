package devices.configuration.device;

import org.assertj.core.api.Assertions;

import static devices.configuration.device.Violations.builder;

public class DeviceSnapshotAssert {
    private final DeviceSnapshot device;

    private DeviceSnapshotAssert(DeviceSnapshot device) {
        this.device = device;
    }

    public static DeviceSnapshotAssert assertThat(Device device) {
        return new DeviceSnapshotAssert(device.toSnapshot());
    }

    public static DeviceSnapshotAssert assertThat(DeviceSnapshot device) {
        return new DeviceSnapshotAssert(device);
    }

    public DeviceSnapshotAssert hasSettings(Settings expected) {
        Assertions.assertThat(device.settings()).isEqualTo(expected);
        return this;
    }

    public DeviceSnapshotAssert hasSettings(Settings.SettingsBuilder expected) {
        Assertions.assertThat(device.settings()).isEqualTo(expected.build());
        return this;
    }

    public DeviceSnapshotAssert hasOwnership(Ownership expected) {
        Assertions.assertThat(device.ownership()).isEqualTo(expected);
        return this;
    }

    public DeviceSnapshotAssert hasLocation(Location expected) {
        Assertions.assertThat(device.location()).isEqualTo(expected);
        return this;
    }

    public DeviceSnapshotAssert hasOpeningHours(OpeningHours expected) {
        Assertions.assertThat(device.openingHours()).isEqualTo(expected);
        return this;
    }

    public DeviceSnapshotAssert hasNoViolations() {
        Assertions.assertThat(device.violations().isValid()).isTrue();
        return this;
    }

    public DeviceSnapshotAssert hasViolations(Violations.ViolationsBuilder violations) {
        Assertions.assertThat(device.violations())
                .isEqualTo(violations.build());
        return this;
    }

    public DeviceSnapshotAssert hasViolationsLikeNotConfiguredDevice() {
        hasViolations(builder()
                .operatorNotAssigned(true)
                .providerNotAssigned(true)
                .locationMissing(true)
                .showOnMapButMissingLocation(false)
                .showOnMapButNoPublicAccess(false)
        );
        return this;
    }
}
