package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;

public class StudentQuestionStatsDto implements Serializable {

    private final Integer userId;

    private final String studentName;

    private final Integer totalStudentQuestions;
    private final Integer approvedStudentQuestions;
    private final Integer rejectedStudentQuestions;

    public StudentQuestionStatsDto(User user) {
        this.userId = user.getId();
        this.studentName = user.getUsername();
        this.totalStudentQuestions = user.getTotalNumberOfStudentQuestions();
        this.approvedStudentQuestions = user.getNumberOfApprovedStudentQuestions();
        this.rejectedStudentQuestions = user.getNumberOfRejectedStudentQuestions();
    }

    public Integer getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Integer getTotalStudentQuestions() {
        return totalStudentQuestions;
    }

    public Integer getApprovedStudentQuestions() {
        return approvedStudentQuestions;
    }

    public Integer getRejectedStudentQuestions() {
        return rejectedStudentQuestions;
    }

    public Integer getPercentageOfStudentQuestions() {
        if(getTotalStudentQuestions() != 0)
            return Math.floorDiv(getApprovedStudentQuestions(), getTotalStudentQuestions());
        else
            return 0;
    }
}
