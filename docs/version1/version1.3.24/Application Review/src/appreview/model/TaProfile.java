package appreview.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TA profile model.
 */
public class TaProfile {
    public String userId;
    public String studentId;
    public String fullName;
    public String major;
    public String year;
    public String department;
    public String phone;
    public String email;
    public double gpa;
    public String cvPath;
    public List<String> skills = new ArrayList<String>();
    public String experienceSummary;
    public Map<String, Object> raw;
}
