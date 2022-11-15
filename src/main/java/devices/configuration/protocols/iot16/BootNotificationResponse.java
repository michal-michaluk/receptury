package devices.configuration.protocols.iot16;

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
