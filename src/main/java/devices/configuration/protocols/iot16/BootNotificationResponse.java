package devices.configuration.protocols.iot16;

import lombok.Builder;

@Builder
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
