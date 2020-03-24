package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;

import java.io.Serializable;

public class DiscussionDto implements Serializable {

    private Integer id;

    private Integer questionId;

    private String messageFromStudent = null;

    private String message = null;

    private String userName = null;

    public DiscussionDto() {}

    public DiscussionDto(Discussion discussion) {
        setId(discussion.getId());
        setQuestionId(discussion.getQuestion().getId());
        setMessageFromStudent(discussion.getMessageFromStudent());
        setMessage(discussion.getTeacherAnswer());
        setUserName(discussion.getStudent().getUsername());
        if (discussion.getTeacher() != null)
            setUserName(discussion.getTeacher().getUsername());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public void setQuestionId(Integer questionId) { this.questionId = questionId;}

    public Integer getQuestionId() { return questionId;}

    public String getMessageFromStudent() { return messageFromStudent; }

    public void setMessageFromStudent(String messageFromStudent) { this.messageFromStudent = messageFromStudent; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
