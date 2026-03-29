package com.example.tasystem.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProfileData {
    public String fullName = "";
    public String studentId = "";
    public String year = "";
    public String programMajor = "";
    public String email = "";
    public String phoneNumber = "";

    public String address = "";
    public String shortBio = "";

    public List<SkillItem> skills = new ArrayList<>();

    public CvInfo cv = new CvInfo();

    public int profileCompletionPercent = 0;
    public int numberOfApplications = 0;

    public SkillItem addSkill(String name, String category, String proficiency) {
        SkillItem item = new SkillItem(
                Objects.requireNonNullElse(name, "").trim(),
                Objects.requireNonNullElse(category, "").trim(),
                Objects.requireNonNullElse(proficiency, "").trim()
        );
        if (skills == null) {
            skills = new ArrayList<>();
        }
        skills.add(item);
        return item;
    }
}
