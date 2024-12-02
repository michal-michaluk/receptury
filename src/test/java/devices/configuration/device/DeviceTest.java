package devices.configuration.device;

import org.junit.jupiter.api.Test;

import static devices.configuration.device.DeviceConfigurationAssert.assertThat;
import static devices.configuration.device.DeviceFixture.*;
import static devices.configuration.device.Violations.builder;

class DeviceTest {

    @Test
    void newDeviceHasSettingsSetToDefaults() {
        Device device = Device.newDevice(DeviceFixture.randomId());

        assertThat(device)
                .hasSettings(Settings.defaultSettings())
                .hasViolationsLikeNotConfiguredDevice()
                .isNotVisible();
    }

    @Test
    void overrideSettings() {
        Device device = givenDevice();

        device.updateSettings(updateSettingsForPublicDeviceOnly());

        assertThat(device)
                .hasSettings(Settings.defaultSettings().toBuilder()
                        .showOnMap(true)
                        .publicAccess(true)
                )
                .hasNoViolations();
    }

    @Test
    void updateSingleValueInSettings() {
        Device device = givenDevice();

        device.updateSettings(updateSettingsWithAutoStartOnly());

        assertThat(device)
                .hasSettings(Settings.defaultSettings().toBuilder()
                        .autoStart(true))
                .hasNoViolations();
    }

    @Test
    void mergeSettings() {
        Device device = givenDevice();

        device.updateSettings(updateSettingsForPublicDeviceOnly());
        device.updateSettings(updateSettingsWithAutoStartOnly());

        assertThat(device)
                .hasSettings(Settings.builder()
                        .publicAccess(true)
                        .showOnMap(true)
                        .autoStart(true)
                        .remoteControl(false)
                        .billing(false)
                        .reimbursement(false)
                )
                .hasNoViolations();
    }

    @Test
    void newDeviceHasOwnershipSetToUnowned() {
        Device device = Device.newDevice(DeviceFixture.randomId());

        assertThat(device)
                .hasOwnership(Ownership.unowned())
                .hasViolationsLikeNotConfiguredDevice();
    }

    @Test
    void assignStationToOwner() {
        Device device = givenDevice();
        device.assignTo(someOtherOwnership());

        assertThat(device)
                .hasOwnership(someOtherOwnership())
                .hasNoViolations();
    }

    @Test
    void resetOwnership() {
        Device device = givenDevice();

        device.assignTo(Ownership.unowned());

        assertThat(device)
                .hasOwnership(Ownership.unowned())
                .hasSettings(Settings.defaultSettings())
                .hasLocation(null)
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasViolationsLikeNotConfiguredDevice();
    }

    @Test
    void resetToDefaults() {
        Device device = givenDevice();
        device.resetConfiguration();

        assertThat(device)
                .hasOwnership(DeviceFixture.ownership())
                .hasSettings(Settings.defaultSettings())
                .hasLocation(null)
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasViolations(violations()
                        .locationMissing(true));
    }

    @Test
    void newDeviceHasNoLocation() {
        Device device = Device.newDevice(DeviceFixture.randomId());

        assertThat(device)
                .hasLocation(null)
                .hasViolationsLikeNotConfiguredDevice();
    }

    @Test
    void updateLocation() {
        Device device = Device.newDevice(DeviceFixture.randomId());
        device.updateLocation(location());

        assertThat(device)
                .hasLocation(location())
                .hasViolations(violations()
                        .providerNotAssigned(true)
                        .operatorNotAssigned(true)
                );
    }

    @Test
    void overrideLocation() {
        Device device = givenDevice();
        device.updateLocation(someOtherLocation());

        assertThat(device)
                .hasLocation(someOtherLocation())
                .hasNoViolations();
    }

    @Test
    void resetLocation() {
        Device device = givenDevice();
        device.updateLocation(null);

        assertThat(device)
                .hasLocation(null)
                .hasViolations(violations()
                        .locationMissing(true));
    }

    private static Violations.ViolationsBuilder violations() {
        return builder()
                .providerNotAssigned(false)
                .operatorNotAssigned(false)
                .locationMissing(false)
                .showOnMapButMissingLocation(false)
                .showOnMapButNoPublicAccess(false);
    }
}
