package com.gaikhamar.app.util;

public final class Access {
    private Access() {}

    public static boolean isAdmin(String role) { return "ADMIN".equals(role); }

    /** Admin and Manager can see money screens. Workers cannot. */
    public static boolean canSeeMoney(String role) { return "ADMIN".equals(role) || "MANAGER".equals(role); }
}
