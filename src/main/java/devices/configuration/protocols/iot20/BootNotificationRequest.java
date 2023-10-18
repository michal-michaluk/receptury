package devices.configuration.protocols.iot20;

import devices.configuration.intervals.DeviceInfo;
import lombok.Value;

@Value
class BootNotificationRequest {
    Device device;
    Reason reason;

    public DeviceInfo toDeviceInfo(String deviceId) {
        return new DeviceInfo(
                deviceId,
                device.getVendorName(),
                device.getModel(),
                device.getFirmwareVersion()
        );
    }

    @Value
    static class Device {
        String serialNumber;
        String model;
        Modem modem;
        String vendorName;
        String firmwareVersion;
    }

    @Value
    static class Modem {
        String iccid;
        String imsi;
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
}
