package com.taapp.model;

import java.util.List;

public class TA {
    private final String id;
    private final String name;
    private final String studentId;
    private final String email;
    private final String program;
    private final String year;
    private final int assignedPositions;
    private final int totalWorkload;
    private final String status;
    private final List<AssignedPosition> positions;

    public TA(
            String id,
            String name,
            String studentId,
            String email,
            String program,
            String year,
            int assignedPositions,
            int totalWorkload,
            String status,
            List<AssignedPosition> positions) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.program = program;
        this.year = year;
        this.assignedPositions = assignedPositions;
        this.totalWorkload = totalWorkload;
        this.status = status;
        this.positions = positions == null ? List.of() : List.copyOf(positions);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStudentId() { return studentId; }
    public String getEmail() { return email; }
    public String getProgram() { return program; }
    public String getYear() { return year; }
    public int getAssignedPositions() { return assignedPositions; }
    public int getTotalWorkload() { return totalWorkload; }
    public String getStatus() { return status; }
    public List<AssignedPosition> getPositions() { return positions; }
}
