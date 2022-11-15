package devices.configuration.device;

import lombok.Builder;

@Builder
record Violations(
        Boolean operatorNotAssigned,
        Boolean providerNotAssigned,
        Boolean locationMissing,
        Boolean showOnMapButMissingLocation,
        Boolean showOnMapButNoPublicAccess) {

    boolean isValid() {
        return !operatorNotAssigned
                && !providerNotAssigned
                && !locationMissing
                && !showOnMapButMissingLocation
                && !showOnMapButNoPublicAccess;
    }
}
