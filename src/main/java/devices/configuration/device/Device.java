package devices.configuration.device;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
class Device {
    final String deviceId;
    private Ownership ownership;
    private Location location;
    private OpeningHours openingHours;
    private Settings settings;

    static Device newDevice(String deviceId) {
        return new Device(
                deviceId,
                Ownership.unowned(),
                null,
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings()
        );
    }

    void assignTo(Ownership ownership) {
        Objects.requireNonNull(ownership);
        if (ownership.isUnowned()) {
            resetConfiguration();
        }
        this.ownership = ownership;
    }

    void resetConfiguration() {
        updateLocation(null);
        updateOpeningHours(OpeningHours.alwaysOpened());
        updateSettings(Settings.defaultSettings());
    }

    void updateLocation(Location location) {
        this.location = location;
    }

    void updateOpeningHours(OpeningHours openingHours) {
        Objects.requireNonNull(openingHours);
        this.openingHours = openingHours;
    }

    void updateSettings(Settings settings) {
        Objects.requireNonNull(settings);
        this.settings = this.settings.merge(settings);
    }

    private Violations checkViolations() {
        return Violations.builder()
                .operatorNotAssigned(ownership.operator() == null)
                .providerNotAssigned(ownership.provider() == null)
                .locationMissing(location == null)
                .showOnMapButMissingLocation(settings.isShowOnMap() && location == null)
                .showOnMapButNoPublicAccess(settings.isShowOnMap() && !settings.isPublicAccess())
                .build();
    }

    DeviceConfiguration toDeviceConfiguration() {
        Violations violations = checkViolations();
        Visibility visibility = Visibility.basedOn(
                violations.isValid() && settings.isPublicAccess(),
                settings.isShowOnMap()
        );
        return new DeviceConfiguration(
                deviceId,
                ownership,
                location,
                openingHours,
                settings,
                violations,
                visibility
        );
    }
}
