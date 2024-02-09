package devices.configuration.tools;

import java.util.Map;
import java.util.Set;

public record Authority(Map<String, Set<String>> roles) {
    public static Authority none() {
        return new Authority(Map.of());
    }

    public static Authority tenant(String tenant, String... roles) {
        return new Authority(Map.of(tenant, Set.of(roles)));
    }

    public boolean hasRole(String tenant, String role) {
        return roles.getOrDefault(tenant, Set.of()).contains(role);
    }
}
