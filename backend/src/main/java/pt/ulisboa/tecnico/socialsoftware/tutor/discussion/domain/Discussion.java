package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "discussions")
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String messageFromStudent = null;

    private String teacherAnswer = null;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    public Discussion() {
    }

    public Discussion(User student, Question question, DiscussionDto dto) {
        this.student = student;
        this.question = question;
        this.messageFromStudent = dto.getMessageFromStudent();
        this.teacherAnswer = dto.getMessage();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getMessageFromStudent() { return messageFromStudent; }

    public void setMessageFromStudent(String messageFromStudent) { this.messageFromStudent = messageFromStudent; }

    public String getTeacherAnswer() { return teacherAnswer; }

    public void setTeacherAnswer(String teacherAnswer) { this.teacherAnswer = teacherAnswer; }

    public User getTeacher() {return teacher; }

    public void setTeacher(User teacher) { this.teacher = teacher; }

    public void updateTeacherAnswer(User teacher, DiscussionDto discussionDto) {
        checkIfDiscussionHasBeenAnswered();
        checkIfTeacherIsEnrolledInQuestionCourseExecution(teacher);
        checkIfAnswerIsEmpty(discussionDto);

        setTeacherAnswer(discussionDto.getMessage());
        setTeacher(teacher);
    }

    private void checkIfAnswerIsEmpty(DiscussionDto discussionDto) {
        String teacherAns = discussionDto.getMessage();
        if (teacherAns == null || teacherAns.isBlank()) {
            throw new TutorException(EMPTY_ANSWER);
        }
    }

    private void checkIfTeacherIsEnrolledInQuestionCourseExecution(User teacher) {
        List<CourseExecution> teacherEnrolledInQuestionCourse = this.getQuestion().getQuizQuestions().stream()
                .map(QuizQuestion::getQuiz)
                .map(Quiz::getCourseExecution)
                .filter(courseExecution -> teacher.getCourseExecutions().contains(courseExecution))
                .collect(Collectors.toList());

        if (teacherEnrolledInQuestionCourse.isEmpty())
            throw new TutorException(TEACHER_NOT_IN_COURSE_EXECUTION);
    }

    private void checkIfDiscussionHasBeenAnswered() {
        if (this.getTeacher() != null)
            throw new TutorException(DISCUSSION_ALREADY_ANSWERED);
    }
}
