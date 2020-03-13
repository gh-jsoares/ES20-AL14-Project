package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StudentQuestionDto implements Serializable {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private Integer id;
    private Integer key;
    private String title;
    private String content;
    private String creationDate = null;
    private String status;
    private Set<OptionDto> options = new HashSet<>();
    private ImageDto image;
    private String creatorUsername;
    private String lastReviewerUsername = null;
    private String reviewedDate = null;

    public StudentQuestionDto() {}

    public StudentQuestionDto(StudentQuestion studentQuestion) {
        this.id = studentQuestion.getId();
        this.key = studentQuestion.getKey();
        this.title = studentQuestion.getTitle();
        this.content = studentQuestion.getContent();
        this.status = studentQuestion.getStatus().name();
        this.options = studentQuestion.getOptions().stream().map(OptionDto::new).collect(Collectors.toSet());
        this.creatorUsername = studentQuestion.getStudent().getUsername();

        populateImage(studentQuestion);
        populateCreationDate(studentQuestion);
        populateReviewedDate(studentQuestion);
        populateLastReviewer(studentQuestion);
    }

    private void populateLastReviewer(StudentQuestion studentQuestion) {
        if (studentQuestion.getLastReviewer() != null)
            this.lastReviewerUsername = studentQuestion.getLastReviewer().getUsername();
    }

    private void populateReviewedDate(StudentQuestion studentQuestion) {
        if (studentQuestion.getReviewedDate() != null)
            this.reviewedDate = studentQuestion.getReviewedDate().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    private void populateCreationDate(StudentQuestion studentQuestion) {
        if (studentQuestion.getCreationDate() != null)
            this.creationDate = studentQuestion.getCreationDate().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    private void populateImage(StudentQuestion studentQuestion) {
        if (studentQuestion.getImage() != null)
            this.image = new ImageDto(studentQuestion.getImage());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getCreationDateAsObject() {
        return LocalDateTime.parse(this.creationDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<OptionDto> getOptions() {
        return options;
    }

    public void setOptions(Set<OptionDto> options) {
        this.options = options;
    }

    public ImageDto getImage() {
        return image;
    }

    public void setImage(ImageDto image) {
        this.image = image;
    }

    public String getCreatorUsername() {
        return this.creatorUsername;
    }

    public String getLastReviewerUsername() {
        return this.lastReviewerUsername;
    }

    @Override
    public String toString() {
        return "StudentQuestionDto{" +
                "id=" + id +
                ", key=" + key +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", options=" + options +
                ", image=" + image +
                ", student=" + creatorUsername +
                '}';
    }

}
