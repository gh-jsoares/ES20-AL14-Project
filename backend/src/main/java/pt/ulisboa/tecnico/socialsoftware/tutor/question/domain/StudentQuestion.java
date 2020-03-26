package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(
        name = "student_questions",
        indexes = {
                @Index(name = "student_question_indx_0", columnList = "key")
        })
public class StudentQuestion {

    @SuppressWarnings("unused")
    public enum Status {
        AWAITING_APPROVAL, ACCEPTED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer key;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private StudentQuestion.Status status = Status.AWAITING_APPROVAL;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "studentQuestion")
    private Image image;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "studentQuestion", fetch = FetchType.EAGER, orphanRemoval=true)
    private Set<Option> options = new HashSet<>();

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToMany(mappedBy = "studentQuestions")
    private Set<Topic> topics = new HashSet<>();

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "last_reviewer_id")
    private User lastReviewer;

    @Column(columnDefinition = "TEXT")
    private String rejectedExplanation;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public StudentQuestion() {

    }

    public StudentQuestion(Course course, User user, StudentQuestionDto studentQuestionDto) {
        checkConsistentStudentQuestion(user, studentQuestionDto);

        this.id = studentQuestionDto.getId();
        this.key = studentQuestionDto.getKey();
        this.content = studentQuestionDto.getContent();
        this.title = studentQuestionDto.getTitle();
        this.status = StudentQuestion.Status.valueOf(studentQuestionDto.getStatus());

        this.course = course;
        course.addStudentQuestion(this);

        this.student = user;
        user.addStudentQuestion(this);

        populateCreationDate(studentQuestionDto);
        populateImage(studentQuestionDto);
        populateOptions(studentQuestionDto);
    }

    private void generateKeys() {
        int max = this.course.getStudentQuestions().stream()
                .filter(studentQuestion -> studentQuestion.key != null)
                .map(StudentQuestion::getKey)
                .max(Comparator.comparing(Integer::valueOf))
                .orElse(0);

        List<StudentQuestion> nullKeyQuestions = this.course.getStudentQuestions().stream()
                .filter(studentQuestion -> studentQuestion.key == null).collect(Collectors.toList());

        for (StudentQuestion studentQuestion: nullKeyQuestions) {
            max = max + 1;
            studentQuestion.key = max;
        }
    }
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Integer getId() {
        return id;
    }

    public Integer getKey() {
        if (this.key == null)
            generateKeys();

        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Set<Option> getOptions() {
        return options;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Set<Topic> getTopics() {
        return this.topics;
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
        topic.addStudentQuestion(this);
    }

    public void removeTopic(Topic topic) {
        topic.removeStudentQuestion(this);
        this.topics.remove(topic);
    }

    public void addOption(Option option) {
        this.options.add(option);
    }

    public void updateTopics(Set<Topic> newTopics) {
        Set<Topic> toRemove = this.topics.stream().filter(topic -> !newTopics.contains(topic)).collect(Collectors.toSet());
        toRemove.forEach(this::removeTopic);
        Set<Topic> toAdd = newTopics.stream().filter(topic -> !this.topics.contains(topic)).collect(Collectors.toSet());
        toAdd.forEach(this::addTopic);
    }

    public LocalDateTime getReviewedDate() {
        return reviewedDate;
    }

    public User getLastReviewer() {
        return lastReviewer;
    }

    public String getRejectedExplanation() {
        return rejectedExplanation;
    }

    public void doApprove(User user) {
        checkUserIsTeacher(user);
        checkAwaitingApproval();

        this.lastReviewer = user;
        this.reviewedDate = LocalDateTime.now();
        this.status = Status.ACCEPTED;

        this.lastReviewer.addReviewedStudentQuestion(this);
    }

    public void doAwait() {
        this.lastReviewer.removeReviewedStudentQuestion(this);

        this.lastReviewer = null;
        this.reviewedDate = null;
        this.status = Status.AWAITING_APPROVAL;
        this.rejectedExplanation = null;
    }

    public void doReject(User user, String rejectedExplanation) {
        checkUserIsTeacher(user);
        checkAwaitingApproval();
        checkRejectedExplanation(rejectedExplanation);

        this.lastReviewer = user;
        this.reviewedDate = LocalDateTime.now();
        this.status = Status.REJECTED;
        this.rejectedExplanation = rejectedExplanation;

        this.lastReviewer.addReviewedStudentQuestion(this);
    }

    private void checkUserIsTeacher(User user) {
        if (user.getRole() != User.Role.TEACHER)
            throw new TutorException(STUDENT_QUESTION_NOT_A_TEACHER);
    }

    public boolean isCreator(User student) {
        return this.getStudent().getUsername().equals(student.getUsername());
    }

    public boolean canTeacherAccess(User teacher) {
        return teacher.getCourseExecutions().stream()
                .anyMatch(c -> c.getCourse().getId().equals(getCourse().getId()));
    }

    private void checkRejectedExplanation(String rejectedExplanation) {
        if (rejectedExplanation == null || rejectedExplanation.trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_REJECT_NO_EXPLANATION);
    }

    private void checkAwaitingApproval() {
        if (!status.equals(Status.AWAITING_APPROVAL))
            throw new TutorException(STUDENT_QUESTION_NOT_AWAITING_APPROVAL, getTitle());
    }

    private void checkConsistentStudentQuestion(User user, StudentQuestionDto studentQuestionDto) {
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(STUDENT_QUESTION_NOT_A_STUDENT);

        if (studentQuestionDto.getTitle() == null || studentQuestionDto.getTitle().trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_TITLE_IS_EMPTY);

        if (studentQuestionDto.getContent() == null || studentQuestionDto.getContent().trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_CONTENT_IS_EMPTY);

        if (studentQuestionDto.getStatus() == null || studentQuestionDto.getStatus().trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_STATUS_IS_EMPTY);

        if (studentQuestionDto.getOptions().stream().anyMatch(optionDto -> optionDto.getContent() == null || optionDto.getContent().trim().length() == 0))
            throw new TutorException(STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY);

        if (studentQuestionDto.getOptions().size() < 4)
            throw new TutorException(TOO_FEW_OPTIONS_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().size() > 4)
            throw new TutorException(TOO_MANY_OPTIONS_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().stream().noneMatch(OptionDto::getCorrect))
            throw new TutorException(NO_CORRECT_OPTION_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().stream().filter(OptionDto::getCorrect).count() != 1)
            throw new TutorException(TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION);
    }

    private void populateCreationDate(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionDto.getCreationDate() != null)
            this.creationDate = LocalDateTime.parse(studentQuestionDto.getCreationDate(), Course.formatter);
        else
            this.creationDate = LocalDateTime.now();
    }

    private void populateImage(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionDto.getImage() != null) {
            Image img = new Image(studentQuestionDto.getImage());
            setImage(img);
            img.setStudentQuestion(this);
        }
    }

    private void populateOptions(StudentQuestionDto studentQuestionDto) {
        int index = 0;
        for (OptionDto optionDto : studentQuestionDto.getOptions()) {
            optionDto.setSequence(index++);
            Option option = new Option(optionDto);
            this.options.add(option);
            option.setStudentQuestion(this);
        }
    }

    public void remove() {
        getCourse().getStudentQuestions().remove(this);
        course = null;
        getTopics().forEach(topic -> topic.removeStudentQuestion(this));
        getTopics().clear();
    }
}
