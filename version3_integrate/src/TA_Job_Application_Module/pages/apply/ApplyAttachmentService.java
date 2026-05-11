package TA_Job_Application_Module.pages.apply;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.TAUser;
import TA_Job_Application_Module.service.DataService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ApplyAttachmentService {
    private ApplyAttachmentService() {
    }

    public static Application.Attachments buildAttachments(TAUser currentUser,
                                                           DataService dataService,
                                                           String cvUploadBase,
                                                           String[] selectedCvPath,
                                                           List<String> selectedSupportPaths) {
        Application.Attachments at = new Application.Attachments();
        if (selectedCvPath != null && selectedCvPath.length > 0
                && selectedCvPath[0] != null && !selectedCvPath[0].isEmpty()) {
            File sourceFile = new File(selectedCvPath[0]);
            String folderName = currentUser.getLoginId();
            if (folderName == null || folderName.isEmpty()) {
                folderName = currentUser.getProfile() == null ? "" : currentUser.getProfile().getStudentId();
            }
            File userDir = new File(cvUploadBase + File.separator + folderName);
            if (!userDir.exists()) {
                userDir.mkdirs();
            }
            File destFile = new File(userDir, sourceFile.getName());
            try {
                java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.io.IOException ex) {
                System.err.println("Error copying CV file: " + ex.getMessage());
            }
            Application.CVInfo cv = new Application.CVInfo();
            cv.setOriginalFileName(sourceFile.getName());
            cv.setStoredFileName(sourceFile.getName());
            cv.setFilePath(destFile.getAbsolutePath());
            String lower = sourceFile.getName().toLowerCase();
            cv.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
            at.setCv(cv);
            syncCurrentUserCvFromFile(currentUser, dataService, destFile, sourceFile.getName());
        } else {
            TAUser.CV existingCv = currentUser == null ? null : currentUser.getCv();
            if (existingCv != null && existingCv.isUploaded()
                    && existingCv.getFilePath() != null && !existingCv.getFilePath().isBlank()) {
                Application.CVInfo cv = new Application.CVInfo();
                String originalName = existingCv.getOriginalFileName();
                if (originalName == null || originalName.isBlank()) {
                    originalName = new File(existingCv.getFilePath()).getName();
                }
                cv.setOriginalFileName(originalName);
                cv.setStoredFileName(existingCv.getStoredFileName() == null ? originalName : existingCv.getStoredFileName());
                cv.setFilePath(existingCv.getFilePath());
                cv.setFileType(existingCv.getFileType());
                at.setCv(cv);
            }
        }

        if (selectedSupportPaths != null && !selectedSupportPaths.isEmpty()) {
            List<Application.Document> docs = new ArrayList<>();
            for (String p : selectedSupportPaths) {
                File f = new File(p);
                Application.Document d = new Application.Document();
                d.setOriginalFileName(f.getName());
                d.setStoredFileName(f.getName());
                d.setFilePath(f.getAbsolutePath());
                String lower = f.getName().toLowerCase();
                d.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
                docs.add(d);
            }
            at.setSupportingDocuments(docs);
        }
        return at;
    }

    private static void syncCurrentUserCvFromFile(TAUser currentUser, DataService dataService, File cvFile, String originalFileName) {
        if (currentUser == null || cvFile == null) {
            return;
        }
        TAUser.CV profileCv = currentUser.getCv();
        if (profileCv == null) {
            profileCv = new TAUser.CV();
            currentUser.setCv(profileCv);
        }
        profileCv.setUploaded(true);
        profileCv.setOriginalFileName(originalFileName);
        profileCv.setStoredFileName(cvFile.getName());
        profileCv.setFilePath(cvFile.getAbsolutePath());
        String lower = cvFile.getName().toLowerCase();
        profileCv.setFileType(lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "");
        profileCv.setFileSizeKB((int) Math.max(1, cvFile.length() / 1024));
        profileCv.setUploadedAt(java.time.LocalDateTime.now().toString());
        dataService.saveCurrentUserToFile();
    }
}
