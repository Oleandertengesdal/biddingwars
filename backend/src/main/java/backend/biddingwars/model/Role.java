package backend.biddingwars.model;

import org.springframework.security.core.GrantedAuthority;

/**
 * Enumeration representing user roles.
 * USER: Regular user with standard permissions.
 * ADMIN: Administrator with elevated permissions.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 26-01-2026
 */
public enum Role implements GrantedAuthority {
    USER,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name(); // Legger til ROLE_ prefiks automatisk for Spring Security
    }
}
