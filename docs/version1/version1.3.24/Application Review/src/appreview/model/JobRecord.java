package appreview.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Job posting model.
 */
public class JobRecord {
    public String jobId;
    public String courseCode;
    public String courseName;
    public String title;
    public String department;
    public int weeklyHours;
    public String lifecycleStatus;
    public String publicationStatus;
    public List<String> preferredSkills = new ArrayList<String>();
    public List<String> responsibilities = new ArrayList<String>();
    public List<String> requirements = new ArrayList<String>();
    public Map<String, Object> raw;
}
