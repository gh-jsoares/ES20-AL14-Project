package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;

public class StudentQuestionStatsDto implements Serializable {

    private final Integer userId;

    private final String studentName;

    private final Long total;
    private final Long approved;
    private final Long rejected;

    public StudentQuestionStatsDto(User user, long total, long approved, long rejected) {
        this.userId = user.getId();
        this.studentName = user.getUsername();
        this.total = total;
        this.approved = approved;
        this.rejected = rejected;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Long getTotal() {
        return total;
    }

    public Long getApproved() {
        return approved;
    }

    public Long getRejected() {
        return rejected;
    }

    public Long getPercentage() {
        if(getTotal() != 0L)
            return Math.round((getApproved().doubleValue() / getTotal()) * 100L);
        else
            return 0L;
    }
}
