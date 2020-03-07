package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;

import java.io.Serializable;

public class DiscussionDto implements Serializable {

    private Integer id;

    private String messageFromStudent = null;

    private String teacherAnswer = null;

    public DiscussionDto() {}

    public DiscussionDto(Discussion discussion) {
        this.id = discussion.getId();
        this.messageFromStudent = discussion.getMessageFromStudent();
        this.teacherAnswer = discussion.getTeacherAnswer();
    }

    public Integer getId() {
        return id;
    }

    public void setId() {
        this.id = id;
    }

    public String getMessageFromStudent() { return messageFromStudent; }

    public void setMessageFromStudent(String messageFromStudent) { this.messageFromStudent = messageFromStudent; }

    public String getTeacherAnswer() { return teacherAnswer; }

    public void setTeacherAnswer(String teacherAnswer) { this.teacherAnswer = teacherAnswer; }
}
