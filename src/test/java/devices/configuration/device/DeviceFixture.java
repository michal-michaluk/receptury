package devices.configuration.device;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class DeviceFixture {

    @NotNull
    public static Ownership ownership() {
        return Ownership.of("operator", "provider");
    }

    @NotNull
    public static Location location() {
        return new Location(
                "Rakietowa",
                "1A",
                "Wrocław",
                "54-621",
                null,
                "POL",
                new Location.Coordinates(new BigDecimal("51.09836221719513"), new BigDecimal("16.931752852309156")));
    }

}
