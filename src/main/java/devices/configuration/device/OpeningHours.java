package devices.configuration.device;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.Value;

import java.time.LocalTime;

import static devices.configuration.device.OpeningHours.OpeningTime.open24h;

@Value
class OpeningHours {
    private final static OpeningHours ALWAYS_OPEN = new OpeningHours(true,
            new Week(open24h(), open24h(), open24h(), open24h(), open24h(), open24h(), open24h())
    );

    boolean alwaysOpen;
    Week opened;

    static OpeningHours alwaysOpened() {
        return ALWAYS_OPEN;
    }

    static OpeningHours openAt(
            OpeningTime monday,
            OpeningTime tuesday,
            OpeningTime wednesday,
            OpeningTime thursday,
            OpeningTime friday,
            OpeningTime saturday,
            OpeningTime sunday) {
        return new OpeningHours(false, new Week(
                monday, tuesday, wednesday, thursday, friday, saturday, sunday)
        );
    }

    static OpeningHours alwaysOpenOrGiven(OpeningHours opening) {
        return opening == null ? OpeningHours.alwaysOpened() : opening;
    }

    @Value
    static class Week {
        OpeningTime monday;
        OpeningTime tuesday;
        OpeningTime wednesday;
        OpeningTime thursday;
        OpeningTime friday;
        OpeningTime saturday;
        OpeningTime sunday;
    }

    @Value
    static class OpeningTime {
        boolean open24h;
        boolean closed;
        @JsonSerialize(using = LocalTimeSerializer.class)
        LocalTime open;
        @JsonSerialize(using = LocalTimeSerializer.class)
        LocalTime close;

        static OpeningTime closed() {
            return new OpeningTime(false, true, null, null);
        }

        static OpeningTime open24h() {
            return new OpeningTime(true, false, null, null);
        }

        static OpeningTime opened(int open, int close) {
            return new OpeningTime(false, false, LocalTime.of(open, 0), LocalTime.of(close, 0));
        }
    }
}
