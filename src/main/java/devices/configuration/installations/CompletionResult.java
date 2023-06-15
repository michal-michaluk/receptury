package devices.configuration.installations;

import devices.configuration.device.Location;
import devices.configuration.device.Ownership;
import lombok.Builder;

@Builder
record CompletionResult(boolean locationIsDefined,
                        boolean bootIsConfirmed,
                        Location location,
                        Ownership ownership) {

    boolean isConfirmed() {
        return locationIsDefined && bootIsConfirmed;
    }
}
