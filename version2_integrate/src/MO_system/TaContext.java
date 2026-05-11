package com.mojobsystem;

/**
 * Placeholder for TA-side flows (loginId 20230001 / 20230002 in data). Used when filtering by applicant user.
 */
public final class TaContext {
    public static final String U_TA_20230001 = "u_ta_20230001";
    public static final String U_TA_20230002 = "u_ta_20230002";

    private static volatile String currentTaUserId = U_TA_20230001;

    private TaContext() {
    }

    public static String getCurrentTaUserId() {
        return currentTaUserId;
    }

    public static void setCurrentTaUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        currentTaUserId = userId;
    }

    /** Parse {@code --ta=1|2|20230001|20230002}. */
    public static void applyMainArgs(String[] args) {
        if (args == null) {
            return;
        }
        for (String a : args) {
            if (a == null) {
                continue;
            }
            String s = a.trim();
            if (s.startsWith("--ta=")) {
                String v = s.substring("--ta=".length()).trim();
                if (v.equals("2") || v.equalsIgnoreCase("20230002") || v.equalsIgnoreCase(U_TA_20230002)) {
                    setCurrentTaUserId(U_TA_20230002);
                } else {
                    setCurrentTaUserId(U_TA_20230001);
                }
            }
        }
    }
}
