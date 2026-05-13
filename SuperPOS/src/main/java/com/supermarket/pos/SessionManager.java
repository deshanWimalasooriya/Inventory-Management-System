package com.supermarket.pos;

public class SessionManager {
    public static String currentUsername;
    public static String currentRole;

    public static void clearSession() {
        currentUsername = null;
        currentRole = null;
    }
}