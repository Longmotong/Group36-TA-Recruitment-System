package TA_Job_Application_Module.model;

import java.util.Locale;

/**
 * Canonical workflow status for MO acceptance awaiting TA/applicant response.
 */
public final class ApplicationStatusCodes {

    public static final String OFFER_PENDING = "offer_pending";

    private ApplicationStatusCodes() {
    }

    public static boolean isOfferPending(String current) {
        if (current == null) {
            return false;
        }
        return OFFER_PENDING.equals(current.trim().toLowerCase(Locale.ROOT));
    }
}
