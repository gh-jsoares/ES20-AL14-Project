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

    public StudentQuestion() {

    }

    public StudentQuestion(User user, StudentQuestionDto studentQuestionDto) {
        // validate input
        checkConsistentStudentQuestion(studentQuestionDto);

        this.title = studentQuestionDto.getTitle();
        this.key = studentQuestionDto.getKey();
        this.content = studentQuestionDto.getContent();
        this.status = StudentQuestion.Status.valueOf(studentQuestionDto.getStatus());

        this.student = user;
        user.addStudentQuestion(this);

        if (studentQuestionDto.getImage() != null) {
            Image img = new Image(studentQuestionDto.getImage());
            setImage(img);
            img.setStudentQuestion(this);
        }

        int index = 0;
        for (OptionDto optionDto : studentQuestionDto.getOptions()) {
            optionDto.setSequence(index++);
            Option option = new Option(optionDto);
            this.options.add(option);
            option.setStudentQuestion(this);
        }
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void setOptions(Set<Option> options) {
        this.options = options;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    private void checkConsistentStudentQuestion(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionDto.getTitle() == null || studentQuestionDto.getTitle().trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_TITLE_IS_EMPTY);

        if (studentQuestionDto.getContent() == null || studentQuestionDto.getContent().trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_CONTENT_IS_EMPTY);

        if (studentQuestionDto.getStatus() == null || studentQuestionDto.getStatus().trim().length() == 0)
            throw new TutorException(STUDENT_QUESTION_STATUS_IS_EMPTY);

        if (studentQuestionDto.getOptions().stream().anyMatch(optionDto -> optionDto.getContent().trim().length() == 0))
            throw new TutorException(STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY);

        if (studentQuestionDto.getOptions().size() != 4)
            throw new TutorException(TOO_FEW_OPTIONS_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().stream().noneMatch(OptionDto::getCorrect))
            throw new TutorException(NO_CORRECT_OPTION_STUDENT_QUESTION);

        if (studentQuestionDto.getOptions().stream().filter(OptionDto::getCorrect).count() != 1)
            throw new TutorException(TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION);

    }
}
