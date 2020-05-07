package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;

public class StudentQuestionStatsDto implements Serializable {

    private final Integer userId;

    private final String studentName;

    private final Long totalStudentQuestions;
    private final Long approvedStudentQuestions;
    private final Long rejectedStudentQuestions;

    public StudentQuestionStatsDto(User user, long total, long approved, long rejected) {
        this.userId = user.getId();
        this.studentName = user.getUsername();
        this.totalStudentQuestions = total;
        this.approvedStudentQuestions = approved;
        this.rejectedStudentQuestions = rejected;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Long getTotalStudentQuestions() {
        return totalStudentQuestions;
    }

    public Long getApprovedStudentQuestions() {
        return approvedStudentQuestions;
    }

    public Long getRejectedStudentQuestions() {
        return rejectedStudentQuestions;
    }

    public Long getPercentageOfStudentQuestions() {
        if(getTotalStudentQuestions() != 0L)
            return Math.round((getApprovedStudentQuestions().doubleValue() / getTotalStudentQuestions()) * 100L);
        else
            return 0L;
    }
}
