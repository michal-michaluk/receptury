package devices.configuration.protocols.iot16;

import devices.configuration.protocols.BootNotification;

import static devices.configuration.protocols.BootNotification.Protocols.IoT16;

record BootNotificationRequest(
        String chargePointVendor,
        String chargePointModel,
        String chargePointSerialNumber,
        String chargeBoxSerialNumber,
        String firmwareVersion,
        String iccid,
        String imsi,
        String meterType,
        String meterSerialNumber) {

    BootNotification toBootNotificationEvent(String deviceId) {
        return new BootNotification(
                deviceId, IoT16,
                chargePointVendor, chargePointModel,
                chargeBoxSerialNumber, firmwareVersion
        );
    }
}
