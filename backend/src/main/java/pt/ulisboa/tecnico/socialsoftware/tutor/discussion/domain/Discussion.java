package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;

import javax.persistence.*;

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

    public Discussion(QuestionAnswer questionAnswer, User student, Question question, DiscussionDto dto) {
        checkMessage(dto.getMessageFromStudent());
        verifyIfAnsweredQuestion(questionAnswer, question.getId(), student.getId());
        setStudent(student);
        setQuestion(question);
        setMessageFromStudent(dto.getMessageFromStudent());
        setTeacherAnswer(dto.getTeacherAnswer());
        question.addDiscussion(this);
        student.addDiscussion(this);
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
        checkMessage(discussionDto.getTeacherAnswer());

        setTeacherAnswer(discussionDto.getTeacherAnswer());
        setTeacher(teacher);
    }

    private void verifyIfAnsweredQuestion(QuestionAnswer questionAnswer, Integer questionId, Integer studentId) {
        if (!questionAnswer.getQuizQuestion().getQuestion().getId().equals(questionId) ||
                !questionAnswer.getQuizAnswer().getUser().getId().equals(studentId))
            throw new TutorException(ErrorMessage.DISCUSSION_QUESTION_NOT_ANSWERED, studentId);
    }

    private void checkMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new TutorException(ErrorMessage.DISCUSSION_MESSAGE_EMPTY);
        }
    }

    private void checkIfTeacherIsEnrolledInQuestionCourseExecution(User teacher) {
        if (this.getQuestion().getQuizQuestions().stream()
                .map(QuizQuestion::getQuiz)
                .map(Quiz::getCourseExecution)
                .noneMatch(courseExecution -> teacher.getCourseExecutions().contains(courseExecution)))
            throw new TutorException(ErrorMessage.TEACHER_NOT_IN_COURSE_EXECUTION);
    }

    private void checkIfDiscussionHasBeenAnswered() {
        if (this.getTeacher() != null)
            throw new TutorException(ErrorMessage.DISCUSSION_ALREADY_ANSWERED);
    }
}
