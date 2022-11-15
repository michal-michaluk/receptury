package devices.configuration.device;

record Ownership(String operator, String provider) {
    public static Ownership unowned() {
        return new Ownership(null, null);
    }
}
