package devices.configuration.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static devices.configuration.device.DeviceFixture.*;
import static devices.configuration.device.DeviceSnapshotAssert.assertThat;
import static devices.configuration.device.Violations.builder;

class DeviceTest {

    @Test
    void newDeviceHasSettingsSetToDefaults() {
        Device device = Device.newDevice(DeviceFixture.randomId());

        assertThat(device)
                .hasSettings(Settings.defaultSettings())
                .hasViolationsLikeNotConfiguredDevice();
    }

    @Test
    void overrideSettings() {
        Device device = givenDevice();

        device.updateSettings(settingsForPublicDevice());

        assertThat(device)
                .hasSettings(settingsForPublicDevice())
                .hasNoViolations();
    }

    @Test
    void updateSingleValueInSettings() throws JsonProcessingException {
        Device device = givenDevice();

        device.updateSettings(settingsWithAutoStartOnly());

        assertThat(device)
                .hasSettings(Settings.defaultSettings().toBuilder()
                        .autoStart(true))
                .hasNoViolations();
    }

    @Test
    void mergeSettings() throws JsonProcessingException {
        Device device = givenDevice();

        device.updateSettings(settingsForPublicDevice());
        device.updateSettings(settingsWithAutoStartOnly());

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
        device.assignTo(someOwnership());

        assertThat(device)
                .hasOwnership(someOwnership())
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
                .hasOpeningHours(OpeningHours.alwaysOpen())
                .hasViolationsLikeNotConfiguredDevice();
    }

    @Test
    void resetToDefaults() {
        Device device = givenDevice();
        device.resetToDefaults();

        assertThat(device)
                .hasOwnership(DeviceFixture.ownership())
                .hasSettings(Settings.defaultSettings())
                .hasLocation(null)
                .hasOpeningHours(OpeningHours.alwaysOpen())
                .hasViolations(Violations.builder()
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
                .hasViolations(builder()
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
                .hasViolations(builder()
                        .locationMissing(true));
    }
}
