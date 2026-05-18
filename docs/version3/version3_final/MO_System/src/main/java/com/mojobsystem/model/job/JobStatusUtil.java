package com.mojobsystem.model.job;

import java.util.Locale;

/**
 * Normalizes job status values across UI, metrics and index persistence.
 */
public final class JobStatusUtil {
    public static final String OPEN = "open";
    public static final String CLOSED = "closed";
    public static final String DRAFT = "draft";

    private JobStatusUtil() {
    }

    /**
     * Canonical status used by persistence/metrics: {@code open|closed|draft}.
     */
    public static String canonical(String status) {
        if (status == null || status.isBlank()) {
            return OPEN;
        }
        String s = status.trim().toLowerCase(Locale.ENGLISH);
        if (CLOSED.equals(s)) {
            return CLOSED;
        }
        if (DRAFT.equals(s)) {
            return DRAFT;
        }
        return OPEN;
    }

    /**
     * UI-facing status label: {@code Open|Closed|Draft}.
     */
    public static String display(String status) {
        return switch (canonical(status)) {
            case CLOSED -> "Closed";
            case DRAFT -> "Draft";
            default -> "Open";
        };
    }

    public static boolean isOpen(String status) {
        return OPEN.equals(canonical(status));
    }
}
