package com.mojobsystem.repository;

import com.mojobsystem.model.Job;

import java.util.List;
import java.util.Locale;

public final class JobIdGenerator {
    private JobIdGenerator() {
    }

    /**
     * Id shape {@code job_EBU6304_2026_spring}, matching files under {@code data/jobs/}.
     */
    public static String nextId(String moduleCode, int year, String termSlug, List<Job> existing) {
        String clean = moduleCode == null ? "MOD" : moduleCode.replaceAll("\\s+", "").toUpperCase(Locale.ENGLISH);
        clean = clean.replaceAll("[^A-Z0-9_-]", "");
        if (clean.isEmpty()) {
            clean = "MOD";
        }
        String base = "job_" + clean + "_" + year + "_" + termSlug;
        if (!containsId(existing, base)) {
            return base;
        }
        for (int i = 2; i < 1000; i++) {
            String candidate = base + "_" + i;
            if (!containsId(existing, candidate)) {
                return candidate;
            }
        }
        return base + "_" + System.currentTimeMillis();
    }

    private static boolean containsId(List<Job> existing, String id) {
        if (existing == null) {
            return false;
        }
        for (Job j : existing) {
            if (id.equals(j.getId())) {
                return true;
            }
        }
        return false;
    }
}
