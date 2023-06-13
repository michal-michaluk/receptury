package devices.configuration.device;

public record Ownership(String operator, String provider) {
    public static Ownership unowned() {
        return new Ownership(null, null);
    }

    public static Ownership of(String operator, String provider) {
        return new Ownership(operator, provider);
    }
}
