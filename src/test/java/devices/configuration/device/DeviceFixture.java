package devices.configuration.device;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class DeviceFixture {

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    @NotNull
    public static Device givenDevice() {
        return new Device(
                randomId(),
                ownership(),
                location(),
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings()
        );
    }

    @NotNull
    public static DeviceConfiguration givenDeviceConfiguration() {
        return givenDeviceConfiguration(randomId());
    }

    @NotNull
    public static DeviceConfiguration givenDeviceConfiguration(String deviceId) {
        return new DeviceConfiguration(
                deviceId,
                ownership(),
                location(),
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings(),
                Violations.builder().build(),
                Visibility.basedOn(true, false)
        );
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

    @NotNull
    public static Location someOtherLocation() {
        return new Location(
                "Żwirki i Wigury",
                "1H",
                "Warszawa",
                "54-202",
                null,
                "PL",
                new Location.Coordinates(
                        new BigDecimal("51.11745363251369"),
                        new BigDecimal("16.997318413019084"))
        );
    }

    @NotNull
    public static Ownership ownership() {
        return new Ownership("Devicex.nl", "public-devices");
    }

    @NotNull
    public static Ownership someOtherOwnership() {
        return new Ownership("Devicex.pl", "public-devices");
    }

    public static Settings updateSettingsWithAutoStartOnly() {
        return Settings.builder()
                .autoStart(true)
                .build();
    }

    public static Settings updateSettingsForPublicDeviceOnly() {
        return Settings.builder()
                .showOnMap(true)
                .publicAccess(true)
                .build();
    }
}
