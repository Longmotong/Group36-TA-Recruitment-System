package com.example.tasystem.integration;

import com.example.tasystem.data.ProfileData;

import java.io.File;
import java.io.IOException;

/**
 * Profile 模块界面与 {@code taportal.TAPortalApp} 之间的桥接：数据以 Job 模块的 {@code TAUser} / JSON 为准。
 */
public interface TaPortalHost {
    String ROUTE_PROFILE = "profile";
    String ROUTE_EDIT_PROFILE = "edit-profile";
    String ROUTE_EDIT_SKILLS = "edit-skills";
    String ROUTE_MANAGE_CV = "manage-cv";

    ProfileData profile();

    void updateProfile(ProfileData next);

    void showRoute(String route);

    /** 主应用仪表盘（Job 模块 Home） */
    void showDashboard();

    void showJobsModule();

    void showApplicationsModule();

    void logout();

    /**
     * 将选择的文件复制到 {@code data/uploads/profile_cv/{studentId}/} 并更新当前用户 CV 字段（与职位申请页逻辑一致）。
     */
    void uploadCvFromFile(File source) throws IOException;

    /** 清除已保存的 profile CV 状态（不强制删除磁盘文件）。 */
    void removeCvUpload();
}
