package devices.configuration.device;

import javax.validation.Valid;

public record UpdateDevice(
        @Valid Location location,
        @Valid OpeningHours openingHours,
        @Valid Settings settings,
        @Valid Ownership ownership) {

    public static UpdateDevice update(Ownership ownership, Location location) {
        return new UpdateDevice(location, null, null, ownership);
    }

    public void apply(Device device) {
        if (location != null) {
            device.updateLocation(location);
        }
        if (openingHours != null) {
            device.updateOpeningHours(openingHours);
        }
        if (settings != null) {
            device.updateSettings(settings);
        }
        if (ownership != null) {
            device.assignTo(ownership);
        }
    }
}
