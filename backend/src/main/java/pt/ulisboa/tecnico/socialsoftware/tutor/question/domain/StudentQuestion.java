package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

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

    @Column(unique=true, nullable = false)
    private Integer key;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(unique=true)
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

    public StudentQuestion() {

    }

    public StudentQuestion(User user, StudentQuestionDto studentQuestionDto) {
        checkConsistentStudentQuestion(user, studentQuestionDto);

        this.id = studentQuestionDto.getId();
        this.key = studentQuestionDto.getKey();
        this.content = studentQuestionDto.getContent();
        this.title = studentQuestionDto.getTitle();
        this.status = StudentQuestion.Status.valueOf(studentQuestionDto.getStatus());

        this.student = user;
        user.addStudentQuestion(this);

        populateCreationDate(studentQuestionDto);
        populateImage(studentQuestionDto);
        populateOptions(studentQuestionDto);
    }

    public Integer getId() {
        return id;
    }

    public Integer getKey() {
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
        checkDuplicateTopic(topic);
        this.topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        checkTopicPresent(topic);
        this.topics.remove(topic);
    }

    public void addOption(Option option) {
        this.options.add(option);
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
        checkAwaitingApproval();
        checkRejectedExplanation(rejectedExplanation);

        this.lastReviewer = user;
        this.reviewedDate = LocalDateTime.now();
        this.status = Status.REJECTED;
        this.rejectedExplanation = rejectedExplanation;

        this.lastReviewer.addReviewedStudentQuestion(this);
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

        if (studentQuestionDto.getOptions().size() != 4)
            throw new TutorException(TOO_FEW_OPTIONS_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().stream().noneMatch(OptionDto::getCorrect))
            throw new TutorException(NO_CORRECT_OPTION_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().stream().filter(OptionDto::getCorrect).count() != 1)
            throw new TutorException(TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION);
    }

    private void checkDuplicateTopic(Topic topic) {
        if(getTopics().stream().anyMatch(t -> t.getId().equals(topic.getId())))
            throw new TutorException(STUDENT_QUESTION_TOPIC_ALREADY_ADDED);
    }

    private void checkTopicPresent(Topic topic) {
        if(getTopics().stream().noneMatch(t -> t.getId().equals(topic.getId())))
            throw new TutorException(STUDENT_QUESTION_TOPIC_NOT_PRESENT);
    }

    private void populateCreationDate(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionDto.getCreationDate() != null)
            this.creationDate = LocalDateTime.parse(studentQuestionDto.getCreationDate());
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
        getTopics().forEach(topic -> topic.getStudentQuestions().remove(this));
        getTopics().clear();
    }
}
