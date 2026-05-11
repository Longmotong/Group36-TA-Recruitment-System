package com.mojobsystem;

/**
 * Current MO user (matches {@code data/indexes/mo_jobs_index.json} keys). Switchable for demo / testing.
 */
public final class MoContext {
    public static final String U_MO_001 = "u_mo_001";
    public static final String U_MO_002 = "u_mo_002";

    private static volatile String currentMoUserId = U_MO_001;

    private MoContext() {
    }

    public static String getCurrentMoUserId() {
        return currentMoUserId;
    }

    public static void setCurrentMoUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        currentMoUserId = userId;
    }

    /** Parse {@code --mo=1|2|mo001|mo002} from main args. */
    public static void applyMainArgs(String[] args) {
        if (args == null) {
            return;
        }
        for (String a : args) {
            if (a == null) {
                continue;
            }
            String s = a.trim();
            if (s.startsWith("--mo=")) {
                String v = s.substring("--mo=".length()).trim();
                if (v.equalsIgnoreCase("2") || v.equalsIgnoreCase("002") || v.equalsIgnoreCase("mo002")
                        || v.equalsIgnoreCase(U_MO_002)) {
                    setCurrentMoUserId(U_MO_002);
                } else {
                    setCurrentMoUserId(U_MO_001);
                }
            }
        }
    }
}
