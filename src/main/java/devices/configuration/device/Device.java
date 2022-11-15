package devices.configuration.device;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class Device {
    final String deviceId;

    private OpeningHours openingHours;
    private Settings settings;

    void updateOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    void updateSettings(Settings settings) {
        this.settings = this.settings.merge(settings);
    }

}
