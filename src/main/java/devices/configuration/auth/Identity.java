package devices.configuration.auth;

import devices.configuration.device.Ownership;

import java.util.Map;
import java.util.Set;

public record Identity(Map<String, Set<String>> roles) {
    public static Identity none() {
        return new Identity(Map.of());
    }

    public static Identity operator(String operator, String... roles) {
        return new Identity(Map.of(operator, Set.of(roles)));
    }

    public Set<String> operators() {
        return roles.keySet();
    }

    public boolean matches(Ownership ownership) {
        return ownership.isOwned() && roles.containsKey(ownership.operator());
    }

    public boolean hasRole(String operator, String role) {
        return roles.getOrDefault(operator, Set.of()).contains(role);
    }
}
