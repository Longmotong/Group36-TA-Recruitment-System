package TA_Job_Application_Module.pages.jobs;

import TA_Job_Application_Module.model.Job;

public class JobsFilterModel {
    private final String search;
    private final String department;
    private final String jobType;

    public JobsFilterModel(String search, String department, String jobType) {
        this.search = search == null ? "" : search.toLowerCase();
        this.department = department;
        this.jobType = jobType;
    }

    public boolean matches(Job job) {
        if (job == null) {
            return false;
        }
        if (!search.isEmpty()) {
            String title = job.getTitle() == null ? "" : job.getTitle().toLowerCase();
            String course = job.getCourseCode() == null ? "" : job.getCourseCode().toLowerCase();
            if (!title.contains(search) && !course.contains(search)) {
                return false;
            }
        }
        if (department != null && !department.equals("All Departments")) {
            if (!department.equals(job.getDepartment())) {
                return false;
            }
        }
        if (jobType != null && !jobType.equals("All Job Types")) {
            String type = job.getEmploymentType() == null ? "" : job.getEmploymentType();
            if (!type.contains(jobType)) {
                return false;
            }
        }
        return true;
    }
}
