package com.example.tasystem.integration;

import com.example.tasystem.data.CvInfo;
import com.example.tasystem.data.ProfileData;
import com.example.tasystem.data.SkillItem;
import taportal.TAUser;

import java.util.ArrayList;
import java.util.List;

public final class TaUserProfileMapper {
    private TaUserProfileMapper() {}

    public static ProfileData toProfileData(TAUser user, int applicationCount) {
        ProfileData p = new ProfileData();
        if (user == null) {
            return p;
        }
        if (user.getProfile() != null) {
            TAUser.Profile pr = user.getProfile();
            p.fullName = nz(pr.getFullName());
            p.studentId = nz(pr.getStudentId());
            p.year = nz(pr.getYear());
            p.programMajor = nz(pr.getProgramMajor());
            p.address = nz(pr.getAddress());
            p.shortBio = nz(pr.getShortBio());
            p.phoneNumber = nz(pr.getPhoneNumber());
        }
        if (user.getAccount() != null) {
            p.email = nz(user.getAccount().getEmail());
        }
        p.skills = skillsFromUser(user.getSkills());
        p.cv = cvFromUser(user.getCv());
        p.profileCompletionPercent = user.getProfileCompletion();
        p.numberOfApplications = applicationCount;
        return p;
    }

    public static void applyToUser(ProfileData p, TAUser user) {
        if (user == null || p == null) {
            return;
        }
        if (user.getProfile() == null) {
            user.setProfile(new TAUser.Profile());
        }
        TAUser.Profile pr = user.getProfile();
        pr.setFullName(p.fullName);
        pr.setStudentId(p.studentId);
        pr.setYear(p.year);
        pr.setProgramMajor(p.programMajor);
        pr.setPhoneNumber(p.phoneNumber);
        pr.setAddress(p.address);
        pr.setShortBio(p.shortBio);
        if (user.getAccount() == null) {
            user.setAccount(new TAUser.Account());
        }
        user.getAccount().setEmail(p.email);
        user.setSkills(skillsToUserSkills(p.skills));
        applyCvInfoToUser(p.cv, user);
    }

    private static void applyCvInfoToUser(com.example.tasystem.data.CvInfo info, TAUser user) {
        TAUser.CV cv = user.getCv();
        if (cv == null) {
            cv = new TAUser.CV();
            user.setCv(cv);
        }
        if (info == null || info.fileName == null || info.fileName.isBlank()) {
            cv.setUploaded(false);
            cv.setOriginalFileName(null);
            cv.setStoredFileName(null);
            cv.setFilePath(null);
            cv.setFileType(null);
            cv.setFileSizeKB(0);
            cv.setUploadedAt(null);
            return;
        }
        cv.setUploaded(true);
        cv.setOriginalFileName(info.fileName);
    }

    private static List<SkillItem> skillsFromUser(TAUser.Skills s) {
        List<SkillItem> out = new ArrayList<>();
        if (s == null) {
            return out;
        }
        addSkills(out, s.getProgramming(), "Programming");
        addSkills(out, s.getTeaching(), "Teaching / Tutoring");
        addSkills(out, s.getCommunication(), "Communication");
        addSkills(out, s.getOther(), "Other Skills");
        return out;
    }

    private static void addSkills(List<SkillItem> out, List<TAUser.Skill> list, String category) {
        if (list == null) {
            return;
        }
        for (TAUser.Skill sk : list) {
            if (sk == null || sk.getName() == null || sk.getName().isBlank()) {
                continue;
            }
            String prof = sk.getProficiency() != null ? sk.getProficiency() : "";
            out.add(new SkillItem(sk.getName().trim(), category, prof));
        }
    }

    private static TAUser.Skills skillsToUserSkills(List<SkillItem> items) {
        TAUser.Skills s = new TAUser.Skills();
        s.setProgramming(new ArrayList<>());
        s.setTeaching(new ArrayList<>());
        s.setCommunication(new ArrayList<>());
        s.setOther(new ArrayList<>());
        if (items == null) {
            return s;
        }
        int seq = 0;
        for (SkillItem it : items) {
            if (it == null || it.name == null || it.name.isBlank()) {
                continue;
            }
            TAUser.Skill sk = new TAUser.Skill();
            sk.setSkillId("skill_" + (++seq) + "_" + System.nanoTime());
            sk.setName(it.name.trim());
            sk.setProficiency(it.proficiency != null ? it.proficiency : "");
            String cat = it.category == null ? "" : it.category.toLowerCase();
            if (cat.contains("program")) {
                s.getProgramming().add(sk);
            } else if (cat.contains("teach")) {
                s.getTeaching().add(sk);
            } else if (cat.contains("comm")) {
                s.getCommunication().add(sk);
            } else {
                s.getOther().add(sk);
            }
        }
        return s;
    }

    private static CvInfo cvFromUser(TAUser.CV cv) {
        CvInfo c = new CvInfo();
        if (cv == null || !cv.isUploaded() || cv.getOriginalFileName() == null || cv.getOriginalFileName().isBlank()) {
            return c;
        }
        c.fileName = cv.getOriginalFileName();
        c.status = "Uploaded";
        String at = cv.getUploadedAt();
        if (at != null && at.length() >= 10) {
            c.lastUpdated = at.substring(0, 10);
        } else {
            c.lastUpdated = nz(at);
        }
        if (cv.getFileSizeKB() > 0) {
            c.sizeLabel = cv.getFileSizeKB() + " KB";
        } else {
            c.sizeLabel = "";
        }
        return c;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
