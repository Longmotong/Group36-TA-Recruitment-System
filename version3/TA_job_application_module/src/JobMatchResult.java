package TA_Job_Application_Module;

public class JobMatchResult {
    public Job job;
    public double score;
    public String analysis;

    public JobMatchResult(Job job, double score, String analysis) {
        this.job = job;
        this.score = score;
        this.analysis = analysis;
    }
}
