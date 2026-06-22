package cl.duoc.libroDigital.attendanceService.security;

import java.util.List;

public class JwtUserPrincipal {

    private final String username;
    private final Long userId;
    private final List<String> roles;

    public JwtUserPrincipal(String username, Long userId, List<String> roles) {
        this.username = username;
        this.userId = userId;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }
}
