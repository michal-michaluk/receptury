package devices.configuration.device;

public record Ownership(String operator, String provider) {

    public static Ownership unowned() {
        return new Ownership(null, null);
    }

    public static Ownership of(String operator, String provider) {
        return new Ownership(operator, provider);
    }

    public Ownership {
        assert isUnowned() || isOwned();
    }

    public boolean isUnowned() {
        return operator == null && provider == null;
    }

    public boolean isOwned() {
        return operator != null && provider != null;
    }
}
