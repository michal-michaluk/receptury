package documentation.generator;

import static documentation.generator.Actor.Type.*;

public record Actor(String name, Actor.Type type) {
    public enum Type {USER, USER_ROLE, SYSTEM, DEVICE, DATABASE}

    public static Actor user(String name) {
        return new Actor(name, USER);
    }

    public static Actor userRole(String name) {
        return new Actor(name, USER_ROLE);
    }

    public static Actor system(String system) {
        return new Actor(system, SYSTEM);
    }

    public static Actor device(String name) {
        return new Actor(name, DEVICE);
    }

    public static Actor database(String name) {
        return new Actor(name, DATABASE);
    }

    @Override
    public String toString() {
        return Serialization.stringify(this);
    }
}
