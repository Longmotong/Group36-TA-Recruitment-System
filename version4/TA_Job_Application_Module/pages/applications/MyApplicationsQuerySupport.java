package TA_Job_Application_Module.pages.applications;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.ApplicationStatusCodes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class MyApplicationsQuerySupport {
    private MyApplicationsQuerySupport() {
    }

    public static String selectedStatusFilterKey(Object selected) {
        if (selected == null) {
            return null;
        }
        return switch (selected.toString()) {
            case "Pending" -> "pending";
            case "Under Review" -> "under_review";
            case "Offer Pending" -> "offer_pending";
            case "Accepted" -> "accepted";
            case "Rejected" -> "rejected";
            default -> null;
        };
    }

    public static boolean matchesFilter(Application app, String statusKey, String queryLower) {
        if (statusKey != null) {
            String current = app.getStatus() != null ? app.getStatus().getCurrent() : "";
            if (ApplicationStatusCodes.OFFER_PENDING.equalsIgnoreCase(statusKey)) {
                if (!ApplicationStatusCodes.isOfferPending(current)) {
                    return false;
                }
            } else if (!statusKey.equalsIgnoreCase(current)) {
                return false;
            }
        }
        if (queryLower == null || queryLower.isEmpty()) {
            return true;
        }
        String title = app.getJobSnapshot() != null ? app.getJobSnapshot().getTitle() : "";
        String dept = app.getJobSnapshot() != null ? app.getJobSnapshot().getDepartment() : "";
        String query = queryLower.toLowerCase(Locale.ROOT);
        return (title != null && title.toLowerCase(Locale.ROOT).contains(query))
                || (dept != null && dept.toLowerCase(Locale.ROOT).contains(query));
    }

    public static String formatDateOnly(String iso, DateTimeFormatter outDate) {
        if (iso == null || iso.length() < 10) {
            return "";
        }
        try {
            return LocalDate.parse(iso.substring(0, 10)).format(outDate);
        } catch (DateTimeParseException e) {
            return iso.substring(0, 10);
        }
    }
}
