package MO_system.ui;

import java.util.Objects;

/**
 * MO shell navigation identifiers. The purple portal bar is wired from {@link MoShellFrame}
 * via {@code profile_module.ui.TaTopNavigationPanel} in MO_THREE portal mode.
 */
public final class NavigationPanel {
    public enum Tab {
        HOME,
        JOB_MANAGEMENT,
        APPLICATION_REVIEW
    }

    public record Actions(Runnable home, Runnable jobManagement, Runnable applicationReview, Runnable logout) {
        public Actions {
            Objects.requireNonNull(home);
            Objects.requireNonNull(jobManagement);
            Objects.requireNonNull(applicationReview);
            Objects.requireNonNull(logout);
        }
    }

    private NavigationPanel() {
    }

    /** Compact MO id for status lines (e.g. {@code u_mo_001} → {@code mo001}). */
    public static String formatMoDisplayId(String moUserId) {
        if (moUserId == null || moUserId.isBlank()) {
            return "-";
        }
        if (moUserId.startsWith("u_mo_")) {
            return "mo" + moUserId.substring("u_mo_".length());
        }
        return moUserId;
    }
}
