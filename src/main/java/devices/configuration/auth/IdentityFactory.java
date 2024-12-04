package devices.configuration.auth;

import org.springframework.security.core.Authentication;

public class IdentityFactory {
    public static Identity of(Authentication authentication) {
        System.out.println(authentication.getAuthorities());
        return Identity.operator("Devicex.nl");
    }
}
