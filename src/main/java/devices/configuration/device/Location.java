package devices.configuration.device;

import java.math.BigDecimal;

record Location(
        String street, String houseNumber,
        String city, String postalCode,
        String state, String country,
        Coordinates coordinates) {

    record Coordinates(BigDecimal longitude,
                       BigDecimal latitude) {
    }
}
