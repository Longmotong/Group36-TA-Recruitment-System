package com.mojobsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Updates {@code data/applications/*.json} without dropping unknown top-level fields
 * (attachments, timeline, workflow, etc.).
 */
public final class ApplicationReviewPersistence {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApplicationReviewPersistence() {
    }

    /**
     * @param decision {@code approved} / {@code rejected} (UI) or {@code accepted} (alias for approved)
     */
    public static void submitReview(Path dataRoot, String applicationId, String decision, String notes, String moUserId)
            throws IOException {
        if (applicationId == null || applicationId.isBlank()) {
            return;
        }
        Path file = dataRoot.resolve("applications").resolve(applicationId + ".json");
        if (!Files.exists(file)) {
            return;
        }
        String d = decision == null ? "" : decision.trim().toLowerCase(Locale.ROOT);
        boolean accept = "approved".equals(d) || "accepted".equals(d);
        boolean reject = "rejected".equals(d);
        if (!accept && !reject) {
            return;
        }
        String current = accept ? "offer_pending" : "rejected";
        String label = accept ? "Offer Pending - Awaiting TA Confirmation" : "Rejected";

        ObjectNode root = (ObjectNode) MAPPER.readTree(file.toFile());
        ObjectNode status = root.withObject("status");
        status.put("current", current);
        status.put("label", label);
        status.put("lastUpdated", LocalDateTime.now().toString());
        status.put("updatedBy", moUserId == null ? "" : moUserId);

        ObjectNode review = root.withObject("review");
        review.put("decision", current);
        review.put("decisionReason", notes == null ? "" : notes.trim());
        review.put("reviewerNotes", notes == null ? "" : notes.trim());
        review.put("reviewedBy", moUserId == null ? "" : moUserId);
        review.put("reviewedAt", LocalDateTime.now().toString());
        review.put("taConfirmationRequired", accept);
        review.put("taConfirmationStatus", accept ? "pending" : "not_required");

        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), root);
    }
}
