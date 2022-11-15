package devices.configuration.protocols.iot20;

import devices.configuration.intervals.DeviceInfo;
import devices.configuration.intervals.Protocols;

record BootNotificationRequest(
        Device device,
        Reason reason) {

    record Device(
            String serialNumber,
            String model,
            Modem modem,
            String vendorName,
            String firmwareVersion) {
    }

    record Modem(String iccid, String imsi) {
    }

    enum Reason {
        ApplicationReset,
        FirmwareUpdate,
        LocalReset,
        PowerUp,
        RemoteReset,
        ScheduledReset,
        Triggered,
        Unknown,
        Watchdog
    }

    DeviceInfo toDevice(String deviceId) {
        return new DeviceInfo(
                deviceId,
                device.vendorName(),
                device.model(),
                Protocols.IoT20
        );
    }
}
