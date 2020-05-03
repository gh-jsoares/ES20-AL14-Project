package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

import java.io.Serializable;

public class DiscussionDto implements Serializable {

    private Integer id;

    private QuestionDto question;

    private boolean visibleToOtherStudents;

    private String messageFromStudent = null;

    private String teacherAnswer = null;

    private String studentName = null;

    private String teacherName = null;

    public DiscussionDto() {}

    public DiscussionDto(Discussion discussion) {
        setId(discussion.getId());
        setVisibleToOtherStudents(discussion.isVisibleToOtherStudents());
        setQuestion(new QuestionDto(discussion.getQuestion()));
        setMessageFromStudent(discussion.getMessageFromStudent());
        setTeacherAnswer(discussion.getTeacherAnswer());
        setStudentName(discussion.getStudent().getUsername());
        if (discussion.getTeacher() != null)
            setTeacherName(discussion.getTeacher().getUsername());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public String getMessageFromStudent() { return messageFromStudent; }

    public void setMessageFromStudent(String messageFromStudent) { this.messageFromStudent = messageFromStudent; }

    public String getTeacherAnswer() { return teacherAnswer; }

    public void setTeacherAnswer(String teacherAnswer) { this.teacherAnswer = teacherAnswer; }

    public QuestionDto getQuestion() { return question; }

    public void setQuestion(QuestionDto question) { this.question = question; }

    public String getStudentName() { return studentName; }

    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTeacherName() { return teacherName; }

    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public void setVisibleToOtherStudents(boolean visibleToOtherStudents) { this.visibleToOtherStudents = visibleToOtherStudents; }
}
