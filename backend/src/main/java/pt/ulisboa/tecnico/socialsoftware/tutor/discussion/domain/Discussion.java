package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.MessageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "discussions")
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "discussion", orphanRemoval=true)
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false)
    private boolean isVisibleToOtherStudents;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private boolean needsAnswer;

    public Discussion() {
    }

    public Discussion(User student, Question question, DiscussionDto dto) {
        checkIfMessages(dto.getMessages());
        setStudent(student);
        setQuestion(question);
        setVisibleToOtherStudents(false);
        setNeedsAnswer(true);
        setMessages(dto.getMessages());
        question.addDiscussion(this);
        student.addDiscussion(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Message> getMessages() { return messages; }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages.stream()
                .map(message -> new Message(this, student, message))
                .collect(Collectors.toList());
    }

    public void addMessage(Message message) { messages.add(message); }

    public User getStudent() { return student; }

    public void setStudent(User student) { this.student = student; }

    public boolean needsAnswer() { return needsAnswer; }

    public void setNeedsAnswer(boolean needsAnswer) { this.needsAnswer = needsAnswer; }

    public boolean isVisibleToOtherStudents() { return isVisibleToOtherStudents; }

    public void setVisibleToOtherStudents(boolean visibleToOtherStudents) { isVisibleToOtherStudents = visibleToOtherStudents; }

    public void checkIfMessages(List<MessageDto> messages) {
        if (messages == null || messages.isEmpty())
            throw new TutorException(ErrorMessage.DISCUSSION_MESSAGE_EMPTY);
    }

    public void updateTeacherAnswer(User teacher, MessageDto messageDto) {
        checkIfTeacherIsEnrolledInQuestionCourseExecution(teacher);
        new Message(this, teacher, messageDto);
        setNeedsAnswer(false);
    }

    private void checkIfTeacherIsEnrolledInQuestionCourseExecution(User teacher) {
        if (this.getQuestion().getQuizQuestions().stream()
                .map(QuizQuestion::getQuiz)
                .map(Quiz::getCourseExecution)
                .noneMatch(courseExecution -> teacher.getCourseExecutions().contains(courseExecution)))
            throw new TutorException(ErrorMessage.TEACHER_NOT_IN_COURSE_EXECUTION);
    }

    private boolean verifiesIfATeacherAnsweredTheDiscussion(User teacher) {
        return messages.stream()
                .map(Message::getUser)
                .anyMatch(u -> u.getId().equals(teacher.getId()));
    }

    public void openDiscussion(User teacher) {
        if (!verifiesIfATeacherAnsweredTheDiscussion(teacher))
            throw new TutorException(ErrorMessage.DISCUSSION_CANT_BE_OPEN);
        else if(isVisibleToOtherStudents)
            throw new TutorException(ErrorMessage.DISCUSSION_ALREADY_OPEN);
        else
            setVisibleToOtherStudents(true);
    }

    public void updateStudentQuestion(User student, MessageDto messageDto) {
        if (!student.getId().equals(this.student.getId()))
            throw new TutorException(ErrorMessage.INVALID_STUDENT, student.getId());
        new Message(this, student, messageDto);
        setNeedsAnswer(true);
    }
}
