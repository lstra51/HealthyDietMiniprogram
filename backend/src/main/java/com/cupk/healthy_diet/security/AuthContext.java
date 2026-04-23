package com.cupk.healthy_diet.security;

public final class AuthContext {
    public static final String USER_ID = "currentUserId";
    public static final String USER_ROLE = "currentUserRole";

    private AuthContext() {
    }

    public static boolean isAdmin(String role) {
        return "admin".equals(role);
    }
}
