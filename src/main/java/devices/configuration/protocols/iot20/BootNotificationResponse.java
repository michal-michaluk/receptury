package devices.configuration.protocols.iot20;

record BootNotificationResponse(
        String currentTime,
        int interval,
        Status status) {

    enum Status {
        Accepted,
        Pending,
        Rejected
    }
}
