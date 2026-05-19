package TA_Job_Application_Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class DataService {
    private static DataService instance;
    private TAUser currentUser;
    private List<Job> jobs;
    private List<Application> applications;
    private int nextApplicationId = 2;
    private final Gson gson;
    
    
    public void initializeUserFromAuth(String username, String role) {
    
        TAUser loaded = loadAuthUserFromFile(username, role);

        if (loaded != null) {
           
            this.currentUser = loaded;
        } else {
            
            this.currentUser = buildPlaceholderUser(username, role);
        }
    }

   
    private TAUser loadAuthUserFromFile(String username, String role) {
        
        String roleLower = (role != null ? role : "TA").toLowerCase();
        if (roleLower.equals("admin") || roleLower.equals("administrator")) {
            roleLower = "admin";
        } else if (roleLower.equals("mo")) {
            roleLower = "mo";
        } else {
            roleLower = "ta";
        }

        String dirPath = DATA_ROOT + File.separator + "users" + File.separator + roleLower + File.separator;
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return null;
        }

        for (File f : files) {
            try (Reader reader = new FileReader(f)) {
                com.google.gson.JsonObject root = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();

                
                com.google.gson.JsonObject acc = root.getAsJsonObject("account");
                if (acc == null) {
                    continue;
                }
                String accUsername = acc.has("username") ? acc.get("username").getAsString() : null;
                if (accUsername == null || !accUsername.equalsIgnoreCase(username)) {
                    continue;
                }

                
                TAUser user = new TAUser();
                user.setUserId(   getStr(root, "userId",   "u_" + username.toLowerCase()));
                user.setLoginId(  getStr(root, "loginId",  username));
                user.setRole(     getStr(root, "role",      roleLower));
                user.setProfileCompletion(getInt(root, "dashboard", "profileCompletion", 0));
                user.setOnboardingCompleted(getBoolNested(root, "dashboard", "onboardingCompleted", true));

                // 2.2.1 account
                TAUser.Account ta = new TAUser.Account();
                ta.setUsername(  getStr(acc, "username",  username));
                ta.setEmail(     getStr(acc, "email",     username + "@university.edu"));
                ta.setStatus(    getStr(acc, "status",    "active"));
                ta.setLastLoginAt(getStr(acc, "lastLoginAt",
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
                user.setAccount(ta);

                // 2.2.2 profile
                com.google.gson.JsonObject prof = root.getAsJsonObject("profile");
                TAUser.Profile tp = new TAUser.Profile();
                tp.setFullName(       getStr(prof, "fullName",       username));
                tp.setStudentId(      getStr(prof, "studentId",     ""));
                tp.setYear(          getStr(prof, "year",           ""));
                tp.setProgramMajor(   getStr(prof, "programMajor",   ""));
                tp.setDepartment(    getStr(prof, "department",     ""));
                tp.setPhoneNumber(    getStr(prof, "phoneNumber",   ""));
                tp.setAddress(       getStr(prof, "address",        ""));
                tp.setShortBio(      getStr(prof, "shortBio",       ""));
                user.setProfile(tp);

                // 2.2.3 academic
                com.google.gson.JsonObject acad = root.getAsJsonObject("academic");
                TAUser.Academic taA = new TAUser.Academic();
                taA.setGpa(getDbl(acad, "gpa", 0.0));
                List<TAUser.CompletedCourse> courses = new ArrayList<>();
                if (acad != null && acad.has("completedCourses")) {
                    for (com.google.gson.JsonElement ce : acad.getAsJsonArray("completedCourses")) {
                        com.google.gson.JsonObject co = ce.getAsJsonObject();
                        TAUser.CompletedCourse cc = new TAUser.CompletedCourse();
                        cc.setCourseCode(getStr(co, "courseCode", ""));
                        cc.setCourseName(getStr(co, "courseName", ""));
                        cc.setGrade(     getStr(co, "grade",     ""));
                        courses.add(cc);
                    }
                }
                taA.setCompletedCourses(courses);
                user.setAcademic(taA);

                // 2.2.4 skills
                com.google.gson.JsonObject skObj = root.getAsJsonObject("skills");
                TAUser.Skills taSk = new TAUser.Skills();
                taSk.setProgramming(   parseSkills(skObj, "programming"));
                taSk.setTeaching(     parseSkills(skObj, "teaching"));
                taSk.setCommunication(parseSkills(skObj, "communication"));
                taSk.setOther(        parseSkills(skObj, "other"));
                user.setSkills(taSk);

                // 2.2.5 CV
                com.google.gson.JsonObject cvObj = root.getAsJsonObject("cv");
                TAUser.CV taCv = new TAUser.CV();
                taCv.setUploaded(       getBool(cvObj, "uploaded",  false));
                taCv.setOriginalFileName(getStr(cvObj,  "originalFileName", ""));
                taCv.setFilePath(       getStr(cvObj,  "filePath",        ""));
                taCv.setUploadedAt(     getStr(cvObj,  "uploadedAt",      ""));
                user.setCv(taCv);

                // 2.2.6 applicationSummary
                com.google.gson.JsonObject sumObj = root.getAsJsonObject("applicationSummary");
                TAUser.ApplicationSummary sum = new TAUser.ApplicationSummary();
                sum.setTotalApplications(getInt(sumObj, "totalApplications", 0));
                sum.setPending(          getInt(sumObj, "pending",          0));
                sum.setUnderReview(      getInt(sumObj, "underReview",      0));
                sum.setAccepted(         getInt(sumObj, "accepted",         0));
                sum.setRejected(         getInt(sumObj, "rejected",         0));
                user.setApplicationSummary(sum);

                // 2.2.7 meta
                com.google.gson.JsonObject metaObj = root.getAsJsonObject("meta");
                TAUser.Meta meta = new TAUser.Meta();
                meta.setCreatedAt(  getStr(metaObj, "createdAt",  ""));
                meta.setUpdatedAt(   getStr(metaObj, "updatedAt",  ""));
                meta.setIsDeleted(  getBool(metaObj, "isDeleted", false));
                meta.setIsActive(   getBool(metaObj, "isActive",  true));
                user.setMeta(meta);

                return user;

            } catch (Exception e) {
               
            }
        }
        return null;
    }

    

    private static String getStr(com.google.gson.JsonObject o, String key, String def) {
        return (o != null && o.has(key) && !o.get(key).isJsonNull()) ? o.get(key).getAsString() : def;
    }

    private static int getInt(com.google.gson.JsonObject o, String key, int def) {
        return (o != null && o.has(key) && !o.get(key).isJsonNull()) ? o.get(key).getAsInt() : def;
    }

    private static int getInt(com.google.gson.JsonObject o, String subKey, String key, int def) {
        if (o == null || !o.has(subKey)) return def;
        com.google.gson.JsonObject sub = o.getAsJsonObject(subKey);
        return (sub != null && sub.has(key) && !sub.get(key).isJsonNull()) ? sub.get(key).getAsInt() : def;
    }

    private static double getDbl(com.google.gson.JsonObject o, String key, double def) {
        return (o != null && o.has(key) && !o.get(key).isJsonNull()) ? o.get(key).getAsDouble() : def;
    }

    private static boolean getBool(com.google.gson.JsonObject o, String key, boolean def) {
        return (o != null && o.has(key) && !o.get(key).isJsonNull()) ? o.get(key).getAsBoolean() : def;
    }

    /** Read boolean from root.subKey.key; if sub-object or key missing, return def. */
    private static boolean getBoolNested(com.google.gson.JsonObject root, String subKey, String key, boolean def) {
        if (root == null || !root.has(subKey)) return def;
        com.google.gson.JsonObject sub = root.getAsJsonObject(subKey);
        return getBool(sub, key, def);
    }

    
    private static List<TAUser.Skill> parseSkills(com.google.gson.JsonObject o, String key) {
        List<TAUser.Skill> list = new ArrayList<>();
        if (o == null || !o.has(key)) return list;
        for (com.google.gson.JsonElement e : o.getAsJsonArray(key)) {
            com.google.gson.JsonObject s = e.getAsJsonObject();
            TAUser.Skill sk = new TAUser.Skill();
            sk.setSkillId(     getStr(s, "skillId",     ""));
            sk.setName(        getStr(s, "name",        ""));
            sk.setProficiency( getStr(s, "proficiency", ""));
            list.add(sk);
        }
        return list;
    }

    
    private TAUser buildPlaceholderUser(String username, String role) {
        TAUser user = new TAUser();
        user.setUserId("u_" + username.toLowerCase());
        user.setLoginId(username);
        user.setRole(role != null ? role.toLowerCase() : "ta");
        user.setProfileCompletion(0);

        TAUser.Account ta = new TAUser.Account();
        ta.setUsername(username);
        ta.setEmail(username + "@university.edu");
        ta.setStatus("active");
        ta.setLastLoginAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        user.setAccount(ta);

        TAUser.Profile tp = new TAUser.Profile();
        tp.setFullName(username);
        tp.setStudentId("");
        user.setProfile(tp);

        TAUser.Academic taA = new TAUser.Academic();
        taA.setGpa(0.0);
        taA.setCompletedCourses(new ArrayList<>());
        user.setAcademic(taA);

        TAUser.Skills taSk = new TAUser.Skills();
        taSk.setProgramming(new ArrayList<>());
        taSk.setTeaching(new ArrayList<>());
        taSk.setCommunication(new ArrayList<>());
        taSk.setOther(new ArrayList<>());
        user.setSkills(taSk);

        TAUser.CV taCv = new TAUser.CV();
        taCv.setUploaded(false);
        user.setCv(taCv);

        TAUser.ApplicationSummary sum = new TAUser.ApplicationSummary();
        sum.setTotalApplications(0);
        sum.setPending(0);
        sum.setUnderReview(0);
        sum.setAccepted(0);
        sum.setRejected(0);
        user.setApplicationSummary(sum);

        TAUser.Meta meta = new TAUser.Meta();
        meta.setCreatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.setIsActive(true);
        user.setMeta(meta);

        return user;
    }
    
   
    private static final String DATA_ROOT;
    
    static {
        
        String binDir = System.getProperty("user.dir");
        
       
        String dataRoot = binDir + File.separator + "data";
        
       
        String dataPath = System.getProperty("taportal.data.path");
        if (dataPath != null && !dataPath.isEmpty()) {
            DATA_ROOT = dataPath;
        } else {
            DATA_ROOT = dataRoot;
        }
        
        JOBS_FOLDER = DATA_ROOT + File.separator + "jobs";
        APPLICATIONS_FOLDER = DATA_ROOT + File.separator + "applications";
        
        
        System.out.println("[DataService] DATA_ROOT = " + DATA_ROOT);
    }
    
    private static final String JOBS_FOLDER;
    private static final String APPLICATIONS_FOLDER;
    
    
    private File findJobsDirectory() {
        File jobsDir = new File(JOBS_FOLDER);
        if (jobsDir.exists() && jobsDir.isDirectory()) {
            File[] files = jobsDir.listFiles((d, name) -> name.endsWith(".json"));
            int fileCount = files != null ? files.length : 0;
            System.out.println("Found jobs directory: " + jobsDir.getAbsolutePath() + " (" + fileCount + " JSON files)");
            return jobsDir;
        }
        System.out.println("Jobs directory not found: " + JOBS_FOLDER);
        return jobsDir;
    }

   
    private File findApplicationsDirectory() {
        File appsDir = new File(APPLICATIONS_FOLDER);
        if (appsDir.exists() && appsDir.isDirectory()) {
            File[] files = appsDir.listFiles((d, name) -> name.endsWith(".json"));
            int fileCount = files != null ? files.length : 0;
            System.out.println("Found applications directory: " + appsDir.getAbsolutePath() + " (" + fileCount + " JSON files)");
            return appsDir;
        }
        System.out.println("Applications directory not found: " + APPLICATIONS_FOLDER);
        return appsDir;
    }
    
   
    private void saveApplicationToFile(Application app) {
        try {
            File dir = findApplicationsDirectory();
           
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, app.getApplicationId() + ".json");
            
           
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            String json = prettyGson.toJson(app);
            
            java.nio.file.Files.writeString(file.toPath(), json);
            System.out.println("Application saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private DataService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadMockData();
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    private void loadMockData() {
        loadCurrentUser();
        loadJobs();
        loadApplications();
    }

    private void loadCurrentUser() {
        currentUser = new TAUser();
        currentUser.setUserId("u_ta_20230001");
        currentUser.setLoginId("20230001");
        currentUser.setRole("ta");
        currentUser.setProfileCompletion(85);

        TAUser.Account account = new TAUser.Account();
        account.setUsername("johnsmith");
        account.setEmail("john.smith@university.edu");
        account.setStatus("active");
        account.setLastLoginAt("2026-03-18T09:20:00");
        currentUser.setAccount(account);

        TAUser.Profile profile = new TAUser.Profile();
        profile.setFullName("John Smith");
        profile.setStudentId("20230001");
        profile.setYear("3rd Year");
        profile.setProgramMajor("Computer Science");
        profile.setDepartment("Computer Science");
        profile.setPhoneNumber("(555) 123-4567");
        profile.setAddress("123 Main St, City, State, ZIP");
        profile.setShortBio("Interested in programming education and software engineering.");
        currentUser.setProfile(profile);

        TAUser.Academic academic = new TAUser.Academic();
        academic.setGpa(3.8);
        List<TAUser.CompletedCourse> courses = new ArrayList<>();
        TAUser.CompletedCourse c1 = new TAUser.CompletedCourse();
        c1.setCourseCode("CS101");
        c1.setCourseName("Introduction to Programming");
        c1.setGrade("A");
        courses.add(c1);
        TAUser.CompletedCourse c2 = new TAUser.CompletedCourse();
        c2.setCourseCode("CS201");
        c2.setCourseName("Data Structures");
        c2.setGrade("A-");
        courses.add(c2);
        academic.setCompletedCourses(courses);
        currentUser.setAcademic(academic);

        TAUser.CV cv = new TAUser.CV();
        cv.setUploaded(true);
        cv.setOriginalFileName("John_Smith_CV.pdf");
        cv.setFilePath("data/uploads/profile_cv/20230001/current_cv.pdf");
        cv.setUploadedAt("2026-03-10T10:30:00");
        currentUser.setCv(cv);

        TAUser.ApplicationSummary summary = new TAUser.ApplicationSummary();
        summary.setTotalApplications(3);
        summary.setPending(1);
        summary.setUnderReview(1);
        summary.setAccepted(1);
        summary.setRejected(0);
        currentUser.setApplicationSummary(summary);
    }

   
    private void loadJobs() {
        jobs = new ArrayList<>();
        
        
        File jobsDir = findJobsDirectory();
        
        if (!jobsDir.exists() || !jobsDir.isDirectory()) {
            System.out.println("Warning: Jobs directory not found at " + jobsDir.getAbsolutePath());
           
            loadJobsFromHardcoded();
            return;
        }
        
       
        File[] jobFiles = jobsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jobFiles == null || jobFiles.length == 0) {
            System.out.println("No job files found in " + jobsDir.getAbsolutePath());
          
            loadJobsFromHardcoded();
            return;
        }
        
       
        for (File jobFile : jobFiles) {
            try (Reader reader = new FileReader(jobFile)) {
                Job job = gson.fromJson(reader, Job.class);
                
                if (isValidAndOpenJob(job)) {
                    jobs.add(job);
                }
            } catch (IOException e) {
                System.err.println("Error reading job file " + jobFile.getName() + ": " + e.getMessage());
            }
        }
        
        
        jobs.sort((j1, j2) -> {
            String date1 = j1.getDates() != null ? j1.getDates().getPostedAt() : "";
            String date2 = j2.getDates() != null ? j2.getDates().getPostedAt() : "";
            return date2.compareTo(date1);
        });
        
        System.out.println("Total jobs loaded from files: " + jobs.size());
    }
    
   
    private boolean isValidAndOpenJob(Job job) {
        if (job == null || job.getJobId() == null) {
            return false;
        }
        
        
        if (job.getLifecycle() != null && job.getLifecycle().isIsDeleted()) {
            return false;
        }
        
        
        if (job.getPublication() != null) {
            String pubStatus = job.getPublication().getStatus();
            if (pubStatus != null && !pubStatus.equalsIgnoreCase("published")) {
                return false;
            }
        }
        
        
        if (job.getLifecycle() != null) {
            String status = job.getLifecycle().getStatus();
            if (status == null || !status.equalsIgnoreCase("open")) {
                return false;
            }
        }
        
        return true;
    }
    
    
    private void loadJobsFromHardcoded() {

        // Job 1
        Job job1 = new Job();
        job1.setJobId("job_CS101_2026_spring");
        job1.setTitle("Introduction to Programming TA");

        Job.Course course1 = new Job.Course();
        course1.setCourseCode("CS 101");
        course1.setCourseName("Introduction to Programming");
        course1.setTerm("Spring");
        course1.setYear(2026);
        job1.setCourse(course1);

        job1.setDepartment("Computer Science");

        Job.Instructor instructor1 = new Job.Instructor();
        instructor1.setName("Dr. Sarah Johnson");
        instructor1.setEmail("sarah.johnson@university.edu");
        job1.setInstructor(instructor1);

        Job.Employment emp1 = new Job.Employment();
        emp1.setJobType("TA");
        emp1.setEmploymentType("Part-time TA");
        emp1.setWeeklyHours(10);
        emp1.setLocationMode("Hybrid");
        emp1.setLocationDetail("On-campus + Remote");
        job1.setEmployment(emp1);

        Job.Dates dates1 = new Job.Dates();
        dates1.setPostedAt("2026-03-17T10:00:00");
        dates1.setDeadline("2026-03-25T23:59:59");
        dates1.setStartDate("2026-04-01");
        dates1.setEndDate("2026-06-30");
        job1.setDates(dates1);

        Job.Content content1 = new Job.Content();
        content1.setSummary("Assist with lab sessions and grading assignments.");
        content1.setDescription("We are seeking a motivated and knowledgeable Teaching Assistant for Introduction to Programming.");
        content1.setResponsibilities(Arrays.asList(
            "Lead weekly lab sessions",
            "Hold office hours",
            "Grade assignments and quizzes",
            "Answer student questions online"
        ));
        content1.setRequirements(Arrays.asList(
            "Completed CS101 with A- or above",
            "Strong Python skills",
            "Good communication skills"
        ));
        content1.setPreferredSkills(Arrays.asList("Python", "Teaching Experience", "Git/GitHub"));
        job1.setContent(content1);

        Job.Lifecycle lifecycle1 = new Job.Lifecycle();
        lifecycle1.setStatus("open");
        lifecycle1.setIsDeleted(false);
        job1.setLifecycle(lifecycle1);

        jobs.add(job1);

        // Job 2
        Job job2 = new Job();
        job2.setJobId("job_CS201_2026_spring");
        job2.setTitle("Data Structures TA");

        Job.Course course2 = new Job.Course();
        course2.setCourseCode("CS 201");
        course2.setCourseName("Data Structures and Algorithms");
        course2.setTerm("Spring");
        course2.setYear(2026);
        job2.setCourse(course2);

        job2.setDepartment("Computer Science");

        Job.Instructor instructor2 = new Job.Instructor();
        instructor2.setName("Prof. Michael Chen");
        instructor2.setEmail("michael.chen@university.edu");
        job2.setInstructor(instructor2);

        Job.Employment emp2 = new Job.Employment();
        emp2.setJobType("TA");
        emp2.setEmploymentType("Part-time TA");
        emp2.setWeeklyHours(15);
        emp2.setLocationMode("On-Campus");
        emp2.setLocationDetail("Computer Science Building");
        job2.setEmployment(emp2);

        Job.Dates dates2 = new Job.Dates();
        dates2.setPostedAt("2026-03-15T10:00:00");
        dates2.setDeadline("2026-03-30T23:59:59");
        dates2.setStartDate("2026-04-01");
        dates2.setEndDate("2026-06-30");
        job2.setDates(dates2);

        Job.Content content2 = new Job.Content();
        content2.setSummary("Help students understand complex data structures and algorithms.");
        content2.setDescription("We are looking for an experienced TA to help students master data structures concepts.");
        content2.setResponsibilities(Arrays.asList(
            "Conduct weekly discussion sessions",
            "Help with programming assignments",
            "Hold tutoring hours",
            "Assist with exam preparation"
        ));
        content2.setRequirements(Arrays.asList(
            "Completed CS201 with A or above",
            "Proficient in Java or C++",
            "Strong problem-solving skills"
        ));
        content2.setPreferredSkills(Arrays.asList("Java", "C++", "Algorithm Design", " tutoring experience"));
        job2.setContent(content2);

        Job.Lifecycle lifecycle2 = new Job.Lifecycle();
        lifecycle2.setStatus("open");
        lifecycle2.setIsDeleted(false);
        job2.setLifecycle(lifecycle2);

        jobs.add(job2);

        // Job 3
        Job job3 = new Job();
        job3.setJobId("job_MATH101_2026_spring");
        job3.setTitle("Calculus I Grading TA");

        Job.Course course3 = new Job.Course();
        course3.setCourseCode("MATH 101");
        course3.setCourseName("Calculus I");
        course3.setTerm("Spring");
        course3.setYear(2026);
        job3.setCourse(course3);

        job3.setDepartment("Mathematics");

        Job.Instructor instructor3 = new Job.Instructor();
        instructor3.setName("Dr. Emily Watson");
        instructor3.setEmail("emily.watson@university.edu");
        job3.setInstructor(instructor3);

        Job.Employment emp3 = new Job.Employment();
        emp3.setJobType("Grading TA");
        emp3.setEmploymentType("Part-time TA");
        emp3.setWeeklyHours(8);
        emp3.setLocationMode("Remote");
        emp3.setLocationDetail("Online grading");
        job3.setEmployment(emp3);

        Job.Dates dates3 = new Job.Dates();
        dates3.setPostedAt("2026-03-16T10:00:00");
        dates3.setDeadline("2026-04-05T23:59:59");
        dates3.setStartDate("2026-04-01");
        dates3.setEndDate("2026-06-30");
        job3.setDates(dates3);

        Job.Content content3 = new Job.Content();
        content3.setSummary("Grade homework and provide feedback to students.");
        content3.setDescription("Help students succeed in their first calculus course by providing timely grading and feedback.");
        content3.setResponsibilities(Arrays.asList(
            "Grade weekly homework assignments",
            "Provide constructive feedback",
            "Answer student questions via email",
            "Report common issues to instructor"
        ));
        content3.setRequirements(Arrays.asList(
            "Completed MATH101 with A- or above",
            "Strong mathematical background",
            "Attention to detail"
        ));
        content3.setPreferredSkills(Arrays.asList("Math", "LaTeX", "Online grading"));
        job3.setContent(content3);

        Job.Lifecycle lifecycle3 = new Job.Lifecycle();
        lifecycle3.setStatus("open");
        lifecycle3.setIsDeleted(false);
        job3.setLifecycle(lifecycle3);

        jobs.add(job3);

        
        Job job4 = new Job();
        job4.setJobId("job_PHYS101_2026_spring");
        job4.setTitle("General Physics I Lab TA");
        Job.Course course4 = new Job.Course();
        course4.setCourseCode("PHYS 101");
        course4.setCourseName("General Physics I");
        course4.setTerm("Spring");
        course4.setYear(2026);
        job4.setCourse(course4);
        job4.setDepartment("Physics");
        Job.Instructor instructor4 = new Job.Instructor();
        instructor4.setName("Dr. James Rivera");
        instructor4.setEmail("james.rivera@university.edu");
        job4.setInstructor(instructor4);
        Job.Employment emp4 = new Job.Employment();
        emp4.setJobType("Lab TA");
        emp4.setEmploymentType("Part-time TA");
        emp4.setWeeklyHours(12);
        emp4.setLocationMode("On-Campus");
        emp4.setLocationDetail("Physics Building Lab");
        job4.setEmployment(emp4);
        Job.Dates dates4 = new Job.Dates();
        dates4.setPostedAt("2026-03-14T09:00:00");
        dates4.setDeadline("2026-04-10T23:59:59");
        dates4.setStartDate("2026-04-01");
        dates4.setEndDate("2026-06-30");
        job4.setDates(dates4);
        Job.Content content4 = new Job.Content();
        content4.setSummary("Supervise introductory physics labs and assist students with experiments.");
        content4.setDescription("Support students in mechanics and thermodynamics lab sections.");
        content4.setResponsibilities(Arrays.asList("Run lab sessions", "Grade lab reports", "Hold office hours"));
        content4.setRequirements(Arrays.asList("Completed PHYS101 with B+ or above", "Strong lab skills"));
        content4.setPreferredSkills(Arrays.asList("Physics", "Teaching", "Safety training"));
        job4.setContent(content4);
        Job.Lifecycle lifecycle4 = new Job.Lifecycle();
        lifecycle4.setStatus("open");
        lifecycle4.setIsDeleted(false);
        job4.setLifecycle(lifecycle4);
        jobs.add(job4);

        
        Job job5 = new Job();
        job5.setJobId("job_CHEM201_2026_spring");
        job5.setTitle("Organic Chemistry Discussion TA");
        Job.Course course5 = new Job.Course();
        course5.setCourseCode("CHEM 201");
        course5.setCourseName("Organic Chemistry");
        course5.setTerm("Spring");
        course5.setYear(2026);
        job5.setCourse(course5);
        job5.setDepartment("Chemistry");
        Job.Instructor instructor5 = new Job.Instructor();
        instructor5.setName("Prof. Anna Mueller");
        instructor5.setEmail("anna.mueller@university.edu");
        job5.setInstructor(instructor5);
        Job.Employment emp5 = new Job.Employment();
        emp5.setJobType("TA");
        emp5.setEmploymentType("Part-time TA");
        emp5.setWeeklyHours(10);
        emp5.setLocationMode("Hybrid");
        emp5.setLocationDetail("Lab + online forum");
        job5.setEmployment(emp5);
        Job.Dates dates5 = new Job.Dates();
        dates5.setPostedAt("2026-03-12T11:00:00");
        dates5.setDeadline("2026-04-08T23:59:59");
        dates5.setStartDate("2026-04-01");
        dates5.setEndDate("2026-06-30");
        job5.setDates(dates5);
        Job.Content content5 = new Job.Content();
        content5.setSummary("Lead weekly discussion sections and review problem sets.");
        content5.setDescription("Help students master organic chemistry concepts and mechanisms.");
        content5.setResponsibilities(Arrays.asList("Discussion sections", "Review sessions", "Grade quizzes"));
        content5.setRequirements(Arrays.asList("Completed CHEM201 with A- or above", "Strong communication"));
        content5.setPreferredSkills(Arrays.asList("Chemistry", "OChem", "Tutoring"));
        job5.setContent(content5);
        Job.Lifecycle lifecycle5 = new Job.Lifecycle();
        lifecycle5.setStatus("open");
        lifecycle5.setIsDeleted(false);
        job5.setLifecycle(lifecycle5);
        jobs.add(job5);
    }

    
    private void loadApplications() {
        applications = new ArrayList<>();

        File appsDir = findApplicationsDirectory();

        if (!appsDir.exists() || !appsDir.isDirectory()) {
            System.out.println("Warning: Applications directory not found at " + appsDir.getAbsolutePath());
            return;
        }

        File[] appFiles = appsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (appFiles == null || appFiles.length == 0) {
            System.out.println("No application files found in " + appsDir.getAbsolutePath());
            return;
        }

        for (File appFile : appFiles) {
            try (Reader reader = new FileReader(appFile)) {
                Application app = gson.fromJson(reader, Application.class);
                
                if (app != null && app.getApplicationId() != null) {
                    applications.add(app);
                    System.out.println("Loaded application from file: " + appFile.getName());
                }
            } catch (IOException e) {
                System.err.println("Error reading application file " + appFile.getName() + ": " + e.getMessage());
            }
        }

        applications.sort((a1, a2) -> {
            String d1 = a1.getMeta() != null ? a1.getMeta().getSubmittedAt() : "";
            String d2 = a2.getMeta() != null ? a2.getMeta().getSubmittedAt() : "";
            return d2.compareTo(d1);
        });

       
        int maxId = 0;
        for (Application app : applications) {
            String appId = app.getApplicationId();
            if (appId != null && appId.startsWith("app_")) {
                try {
                    int num = Integer.parseInt(appId.substring(4));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        nextApplicationId = maxId + 1;

        System.out.println("Total applications loaded from files: " + applications.size());
        System.out.println("Next application ID will be: " + nextApplicationId);
    }

    private boolean isApplicationDeleted(Application app) {
        return app.getMeta() != null && app.getMeta().isIsDeleted();
    }

    // Getters
    public TAUser getCurrentUser() {
        return currentUser;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    
    public void reloadJobs() {
        jobs = new ArrayList<>();

        File jobsDir = findJobsDirectory();

        if (!jobsDir.exists() || !jobsDir.isDirectory()) {
            loadJobsFromHardcoded();
            return;
        }

        File[] jobFiles = jobsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jobFiles == null || jobFiles.length == 0) {
            loadJobsFromHardcoded();
            return;
        }

        for (File jobFile : jobFiles) {
            try (Reader reader = new FileReader(jobFile)) {
                Job job = gson.fromJson(reader, Job.class);
                if (isValidAndOpenJob(job)) {
                    jobs.add(job);
                }
            } catch (IOException e) {
                System.err.println("Error reading job file " + jobFile.getName() + ": " + e.getMessage());
            }
        }

        jobs.sort((j1, j2) -> {
            String date1 = j1.getDates() != null ? j1.getDates().getPostedAt() : "";
            String date2 = j2.getDates() != null ? j2.getDates().getPostedAt() : "";
            return date2.compareTo(date1);
        });

        System.out.println("[DataService] Jobs reloaded from files: " + jobs.size());
    }

    public List<Job> getOpenJobs() {
        return jobs.stream()
            .filter(j -> "open".equals(j.getStatus()))
            .toList();
    }

    public List<Application> getApplications() {
        return applications;
    }

   
    public List<Application> getUserApplications() {
       
        reloadApplicationsFromFile();
        return applications.stream()
            .filter(a -> currentUser.getUserId().equals(a.getUserId())
                        && !a.isDraft()
                        && !isApplicationCancelled(a))
            .toList();
    }

    
    private void reloadApplicationsFromFile() {
        applications.clear();

        File appsDir = new File(APPLICATIONS_FOLDER);
        if (!appsDir.exists() || !appsDir.isDirectory()) {
            return;
        }

        File[] appFiles = appsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (appFiles == null || appFiles.length == 0) {
            return;
        }

        for (File appFile : appFiles) {
            try (Reader reader = new FileReader(appFile)) {
                Application app = gson.fromJson(reader, Application.class);
                if (app != null && app.getApplicationId() != null) {
                    applications.add(app);
                }
            } catch (IOException e) {
                System.err.println("Error reading application file " + appFile.getName() + ": " + e.getMessage());
            }
        }

        
        applications.sort((a1, a2) -> {
            String d1 = a1.getMeta() != null ? a1.getMeta().getSubmittedAt() : "";
            String d2 = a2.getMeta() != null ? a2.getMeta().getSubmittedAt() : "";
            return d2.compareTo(d1);
        });
    }

   
    public boolean hasAppliedToJob(String jobId) {
        return applications.stream()
            .anyMatch(a -> currentUser.getUserId().equals(a.getUserId())
                        && jobId.equals(a.getJobId())
                        && !a.isDraft()
                        && !isApplicationCancelled(a));
    }

   
    private boolean isApplicationCancelled(Application app) {
        if (app.getMeta() != null && app.getMeta().isIsDeleted()) {
            return true;
        }
        String status = app.getStatus() != null ? app.getStatus().getCurrent() : "";
        return "cancelled".equalsIgnoreCase(status);
    }

    public Job getJobById(String jobId) {
        return jobs.stream()
            .filter(j -> j.getJobId().equals(jobId))
            .findFirst()
            .orElse(null);
    }

    
    public TAUser loadUserData(String username, String role) {
        return loadAuthUserFromFile(username, role);
    }

    
    public void saveCurrentUserToFile() {
        if (currentUser == null) return;
        saveUserToFile(currentUser);
    }

    
    public void saveUserToFile(TAUser user) {
        if (user == null) return;
        String roleLower = (user.getRole() != null ? user.getRole() : "ta").toLowerCase();
        if (roleLower.equals("admin") || roleLower.equals("administrator")) {
            roleLower = "admin";
        } else if (roleLower.equals("mo")) {
            roleLower = "mo";
        } else {
            roleLower = "ta";
        }

        String dirPath = DATA_ROOT + File.separator + "users" + File.separator + roleLower + File.separator;
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            System.out.println("[DataService] User directory not found: " + dirPath);
            return;
        }

        
        File targetFile = null;
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try (Reader reader = new FileReader(f)) {
                    com.google.gson.JsonObject root = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
                    com.google.gson.JsonObject acc = root.getAsJsonObject("account");
                    if (acc != null) {
                        String accUsername = acc.has("username") ? acc.get("username").getAsString() : null;
                        if (accUsername != null && accUsername.equalsIgnoreCase(user.getAccount() != null ? user.getAccount().getUsername() : "")) {
                            targetFile = f;
                            break;
                        }
                    }
                } catch (Exception e) {
                    
                }
            }
        }

        if (targetFile == null) {
            System.out.println("[DataService] User file not found for: " + (user.getAccount() != null ? user.getAccount().getUsername() : "unknown"));
            return;
        }

        try {
            
            com.google.gson.JsonObject root;
            try (Reader reader = new FileReader(targetFile)) {
                root = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
            }

            
            if (user.getProfile() != null) {
                com.google.gson.JsonObject prof = new com.google.gson.JsonObject();
                prof.addProperty("fullName", nvl(user.getProfile().getFullName()));
                prof.addProperty("studentId", nvl(user.getProfile().getStudentId()));
                prof.addProperty("year", nvl(user.getProfile().getYear()));
                prof.addProperty("programMajor", nvl(user.getProfile().getProgramMajor()));
                prof.addProperty("department", nvl(user.getProfile().getDepartment()));
                prof.addProperty("phoneNumber", nvl(user.getProfile().getPhoneNumber()));
                prof.addProperty("address", nvl(user.getProfile().getAddress()));
                prof.addProperty("shortBio", nvl(user.getProfile().getShortBio()));
                root.add("profile", prof);
            }

            
            if (user.getAcademic() != null) {
                com.google.gson.JsonObject acad = new com.google.gson.JsonObject();
                acad.addProperty("gpa", user.getAcademic().getGpa());
                com.google.gson.JsonArray coursesArray = new com.google.gson.JsonArray();
                if (user.getAcademic().getCompletedCourses() != null) {
                    for (TAUser.CompletedCourse cc : user.getAcademic().getCompletedCourses()) {
                        com.google.gson.JsonObject co = new com.google.gson.JsonObject();
                        co.addProperty("courseCode", nvl(cc.getCourseCode()));
                        co.addProperty("courseName", nvl(cc.getCourseName()));
                        co.addProperty("grade", nvl(cc.getGrade()));
                        coursesArray.add(co);
                    }
                }
                acad.add("completedCourses", coursesArray);
                root.add("academic", acad);
            }

            
            if (user.getSkills() != null) {
                com.google.gson.JsonObject skObj = new com.google.gson.JsonObject();
                skObj.add("programming", skillsToArray(user.getSkills().getProgramming()));
                skObj.add("teaching", skillsToArray(user.getSkills().getTeaching()));
                skObj.add("communication", skillsToArray(user.getSkills().getCommunication()));
                skObj.add("other", skillsToArray(user.getSkills().getOther()));
                root.add("skills", skObj);
            }

            
            if (user.getCv() != null) {
                com.google.gson.JsonObject cvObj = new com.google.gson.JsonObject();
                cvObj.addProperty("uploaded", user.getCv().isUploaded());
                cvObj.addProperty("originalFileName", nvl(user.getCv().getOriginalFileName()));
                cvObj.addProperty("storedFileName", nvl(user.getCv().getStoredFileName()));
                cvObj.addProperty("filePath", nvl(user.getCv().getFilePath()));
                cvObj.addProperty("fileType", nvl(user.getCv().getFileType()));
                cvObj.addProperty("fileSizeKB", user.getCv().getFileSizeKB());
                cvObj.addProperty("uploadedAt", nvl(user.getCv().getUploadedAt()));
                root.add("cv", cvObj);
            }

            
            if (user.getAccount() != null) {
                com.google.gson.JsonObject accObj = root.getAsJsonObject("account");
                if (accObj == null) {
                    accObj = new com.google.gson.JsonObject();
                    root.add("account", accObj);
                }
                
                accObj.addProperty("email", nvl(user.getAccount().getEmail()));
            }

            
            com.google.gson.JsonObject dashObj = root.getAsJsonObject("dashboard");
            if (dashObj == null) {
                dashObj = new com.google.gson.JsonObject();
            }
            dashObj.addProperty("profileCompletion", user.getProfileCompletion());
            dashObj.addProperty("onboardingCompleted", user.isOnboardingCompleted());
            root.add("dashboard", dashObj);

            
            com.google.gson.JsonObject meta = root.getAsJsonObject("meta");
            if (meta == null) {
                meta = new com.google.gson.JsonObject();
                root.add("meta", meta);
            }
            meta.addProperty("updatedAt", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            String json = prettyGson.toJson(root);
            java.nio.file.Files.writeString(targetFile.toPath(), json);
            System.out.println("[DataService] User data saved to: " + targetFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("[DataService] Error saving user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private com.google.gson.JsonArray skillsToArray(java.util.List<TAUser.Skill> skills) {
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        if (skills != null) {
            for (TAUser.Skill s : skills) {
                com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
                obj.addProperty("skillId", nvl(s.getSkillId()));
                obj.addProperty("name", nvl(s.getName()));
                obj.addProperty("proficiency", nvl(s.getProficiency()));
                arr.add(obj);
            }
        }
        return arr;
    }

    
    private String nvl(String s) {
        return s != null ? s : "";
    }

    public Application getApplicationById(String applicationId) {
        return applications.stream()
            .filter(a -> a.getApplicationId().equals(applicationId))
            .findFirst()
            .orElse(null);
    }

    public void addApplication(Application app) {
        app.setApplicationId("app_" + String.format("%06d", nextApplicationId++));
        app.setStudentId(currentUser.getProfile().getStudentId());
        app.setUserId(currentUser.getUserId());
        
        Application.Status status = new Application.Status();
        status.setCurrent("pending");
        status.setLabel("Pending");
        status.setColor("yellow");
        status.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        app.setStatus(status);

        List<Application.TimelineEvent> timeline = new ArrayList<>();
        Application.TimelineEvent event = new Application.TimelineEvent();
        event.setTimelineId("tl_" + System.currentTimeMillis());
        event.setStepKey("submitted");
        event.setStepLabel("Application Submitted");
        event.setStatus("completed");
        event.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        event.setNote("Application submitted successfully.");
        timeline.add(event);
        app.setTimeline(timeline);

        Application.Meta meta = new Application.Meta();
        meta.setSubmittedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.setIsDeleted(false);
        app.setMeta(meta);

        Application.Review review = new Application.Review();
        review.setStatusMessage("Your application has been submitted and is waiting for review.");
        review.setNextSteps("Please wait for further updates from the department.");
        app.setReview(review);

        
        String moId = loadMoIdFromJob(app.getJobId());
        if (moId != null && !moId.isEmpty()) {
            Application.Workflow workflow = new Application.Workflow();
            workflow.setAssignedMO(moId);
            app.setWorkflow(workflow);
        }

        applications.add(0, app);
        
        
        saveApplicationToFile(app);

        
        TAUser.ApplicationSummary summary = currentUser.getApplicationSummary();
        summary.setTotalApplications(summary.getTotalApplications() + 1);
        summary.setPending(summary.getPending() + 1);
    }

   
    public boolean cancelApplication(String applicationId) {
        Application app = getApplicationById(applicationId);
        if (app == null) {
            return false;
        }
        String cur = app.getStatus() != null ? app.getStatus().getCurrent() : "";
        if (!"pending".equalsIgnoreCase(cur)) {
            return false;
        }

        app.getStatus().setCurrent("cancelled");
        app.getStatus().setLabel("Cancelled");
        app.getStatus().setColor("gray");
        app.getStatus().setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (app.getMeta() != null) {
            app.getMeta().setIsDeleted(true);
            app.getMeta().setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        List<Application.TimelineEvent> timeline = app.getTimeline();
        if (timeline == null) {
            timeline = new ArrayList<>();
            app.setTimeline(timeline);
        }
        Application.TimelineEvent ev = new Application.TimelineEvent();
        ev.setTimelineId("tl_" + System.currentTimeMillis());
        ev.setStepKey("cancelled");
        ev.setStepLabel("Application Withdrawn");
        ev.setStatus("completed");
        ev.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        ev.setNote("Application was withdrawn by the applicant.");
        timeline.add(ev);

        saveApplicationToFile(app);
        return true;
    }

    public int countApplicationsByStatus(String status) {
        return (int) getUserApplications().stream()
            .filter(a -> status.equals(a.getStatus().getCurrent()))
            .count();
    }

   
    public List<Application> getDrafts() {
        reloadApplicationsFromFile();
        return applications.stream()
            .filter(a -> currentUser.getUserId().equals(a.getUserId()) && a.isDraft())
            .toList();
    }

   
    public String saveDraft(Application draftApp, Job job) {
       
        Application existing = applications.stream()
            .filter(a -> currentUser.getUserId().equals(a.getUserId())
                        && job.getJobId().equals(a.getJobId())
                        && a.isDraft())
            .findFirst().orElse(null);

        if (existing != null) {
            
            existing.setApplicantSnapshot(draftApp.getApplicantSnapshot());
            existing.setApplicationForm(draftApp.getApplicationForm());
            existing.setAttachments(draftApp.getAttachments());
            if (existing.getMeta() != null) {
                existing.getMeta().setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            saveApplicationToFile(existing);
            return existing.getApplicationId();
        } else {
            
            Application newDraft = new Application();
            newDraft.setApplicationId("app_" + String.format("%06d", nextApplicationId++));
            newDraft.setStudentId(currentUser.getProfile().getStudentId());
            newDraft.setUserId(currentUser.getUserId());
            newDraft.setJobId(job.getJobId());
            newDraft.setJobSnapshot(buildJobSnapshot(job));
            newDraft.setApplicantSnapshot(draftApp.getApplicantSnapshot());
            newDraft.setApplicationForm(draftApp.getApplicationForm());
            newDraft.setAttachments(draftApp.getAttachments());
            newDraft.setDraft(true);

            Application.Status status = new Application.Status();
            status.setCurrent("draft");
            status.setLabel("Draft");
            status.setColor("gray");
            status.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            newDraft.setStatus(status);

            Application.Meta meta = new Application.Meta();
            meta.setSubmittedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            meta.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            meta.setIsDeleted(false);
            newDraft.setMeta(meta);

            newDraft.setReview(new Application.Review());

            applications.add(0, newDraft);
            saveApplicationToFile(newDraft);
            return newDraft.getApplicationId();
        }
    }

    
    public boolean submitDraft(String applicationId) {
        Application app = getApplicationById(applicationId);
        if (app == null || !app.isDraft()) {
            return false;
        }

        app.setDraft(false);
        app.getStatus().setCurrent("pending");
        app.getStatus().setLabel("Pending");
        app.getStatus().setColor("yellow");
        app.getStatus().setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Set workflow.assignedMO from job for MO visibility
        String moId = loadMoIdFromJob(app.getJobId());
        if (moId != null && !moId.isEmpty()) {
            Application.Workflow workflow = new Application.Workflow();
            workflow.setAssignedMO(moId);
            app.setWorkflow(workflow);
        }

        List<Application.TimelineEvent> timeline = new ArrayList<>();
        Application.TimelineEvent event = new Application.TimelineEvent();
        event.setTimelineId("tl_" + System.currentTimeMillis());
        event.setStepKey("submitted");
        event.setStepLabel("Application Submitted");
        event.setStatus("completed");
        event.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        event.setNote("Application submitted successfully.");
        timeline.add(event);
        app.setTimeline(timeline);

        if (app.getMeta() != null) {
            app.getMeta().setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        saveApplicationToFile(app);

        TAUser.ApplicationSummary summary = currentUser.getApplicationSummary();
        summary.setTotalApplications(summary.getTotalApplications() + 1);
        summary.setPending(summary.getPending() + 1);

        return true;
    }

   
    public boolean deleteDraft(String applicationId) {
        Application app = getApplicationById(applicationId);
        if (app == null) {
            return false;
        }
        applications.remove(app);
        
        
        try {
            File appFile = new File(APPLICATIONS_FOLDER + File.separator + applicationId + ".json");
            if (appFile.exists()) {
                appFile.delete();
                System.out.println("[DataService] Draft file deleted: " + appFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[DataService] Error deleting draft file: " + e.getMessage());
        }
        
        return true;
    }

    private Application.JobSnapshot buildJobSnapshot(Job job) {
        Application.JobSnapshot snap = new Application.JobSnapshot();
        snap.setTitle(job.getTitle());
        snap.setCourseCode(job.getCourseCode());
        if (job.getCourse() != null) snap.setCourseName(job.getCourse().getCourseName());
        snap.setDepartment(job.getDepartment());
        snap.setInstructorName(job.getInstructorName());
        snap.setInstructorEmail(job.getInstructorEmail());
        snap.setDeadline(job.getDeadlineDisplay());
        snap.setEmploymentType(job.getEmploymentType());
        if (job.getEmployment() != null) {
            snap.setWeeklyHours(job.getEmployment().getWeeklyHours());
            snap.setLocationDetail(job.getEmployment().getLocationDetail());
        }
        snap.setLocationMode(job.getLocationMode());
        return snap;
    }

    
    private String loadMoIdFromJob(String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            return null;
        }

        try {
            File jobFile = new File(JOBS_FOLDER + File.separator + jobId + ".json");
            if (!jobFile.exists()) {
                System.out.println("[DataService] Job file not found: " + jobFile.getAbsolutePath());
                return null;
            }

            try (Reader reader = new FileReader(jobFile)) {
                com.google.gson.JsonObject root = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();

                
                com.google.gson.JsonObject ownership = root.getAsJsonObject("ownership");
                if (ownership != null && ownership.has("managedBy")) {
                    com.google.gson.JsonArray managedBy = ownership.getAsJsonArray("managedBy");
                    if (managedBy != null && managedBy.size() > 0) {
                        String moId = managedBy.get(0).getAsString();
                        System.out.println("[DataService] Loaded MO ID from job: " + moId);
                        return moId;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DataService] Error loading MO ID from job: " + e.getMessage());
        }

        return null;
    }
}


