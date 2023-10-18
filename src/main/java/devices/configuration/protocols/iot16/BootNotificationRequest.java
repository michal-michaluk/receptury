package devices.configuration.protocols.iot16;

import devices.configuration.intervals.DeviceInfo;
import lombok.Value;

@Value
class BootNotificationRequest {
    String chargePointVendor;
    String chargePointModel;
    String chargePointSerialNumber;
    String chargeBoxSerialNumber;
    String firmwareVersion;
    String iccid;
    String imsi;
    String meterType;
    String meterSerialNumber;

    public DeviceInfo toDeviceInfo(String deviceId) {
        return new DeviceInfo(
                deviceId,
                chargePointVendor,
                chargePointModel,
                firmwareVersion
        );
    }
}
