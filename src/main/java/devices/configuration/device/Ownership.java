package devices.configuration.device;

public record Ownership(String operator, String provider) {
    static Ownership unowned() {
        return new Ownership(null, null);
    }
}
