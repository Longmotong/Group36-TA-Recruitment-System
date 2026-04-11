package com.taapp.data;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class AuditLogService {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_PATH = Paths.get("logs", "operation_audit.log");

    private AuditLogService() {}

    /**
     * Backward-compatible simple logging.
     */
    public static synchronized void log(String actor, String action, String target, String detail) {
        logCompliance(
                actor,
                action,
                target,
                detail,
                autoOperatorId(actor),
                autoTerminalId(),
                generateRequestId(),
                "-",
                "-",
                "-"
        );
    }

    /**
     * Compliance-grade full-chain logging.
     */
    public static synchronized void logCompliance(
            String actor,
            String action,
            String target,
            String detail,
            String operatorId,
            String terminalId,
            String requestId,
            String beforeValue,
            String afterValue,
            String changeDiff) {
        try {
            ensureLogFile();

            String line = String.format(
                    "[%s] actor=%s | operatorId=%s | terminalId=%s | requestId=%s | action=%s | target=%s | detail=%s | before=%s | after=%s | diff=%s%n",
                    TS.format(LocalDateTime.now()),
                    safe(actor),
                    safe(operatorId),
                    safe(terminalId),
                    safe(requestId),
                    safe(action),
                    safe(target),
                    safe(detail),
                    safe(beforeValue),
                    safe(afterValue),
                    safe(changeDiff)
            );

            Files.writeString(
                    LOG_PATH,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
        }
    }

    public static synchronized List<String> readRecent(int maxLines) {
        try {
            ensureLogFile();
            List<String> lines = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);
            if (lines.size() <= maxLines) return lines;
            return new ArrayList<>(lines.subList(lines.size() - maxLines, lines.size()));
        } catch (IOException e) {
            return Collections.singletonList("[log unavailable] " + e.getMessage());
        }
    }

    public static String generateRequestId() {
        return "req-" + UUID.randomUUID();
    }

    private static String autoOperatorId(String actor) {
        if (actor == null || actor.isBlank()) return "unknown-operator";
        return "op-" + actor.trim().toLowerCase().replace(" ", "_");
    }

    private static String autoTerminalId() {
        String user = System.getProperty("user.name", "unknown-user");
        String host = "unknown-host";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
        }
        return user + "@" + host;
    }

    private static void ensureLogFile() throws IOException {
        Path parent = LOG_PATH.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
        if (!Files.exists(LOG_PATH)) Files.createFile(LOG_PATH);
    }

    private static String safe(String s) {
        return s == null ? "-" : s.replace("\n", " ").replace("\r", " ");
    }
}
