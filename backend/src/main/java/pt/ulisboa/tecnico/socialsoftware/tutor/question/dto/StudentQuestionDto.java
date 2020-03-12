package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StudentQuestionDto implements Serializable {
    private Integer id;
    private Integer key;
    private String title;
    private String content;
    private String creationDate = null;
    private String status;
    private Set<OptionDto> options = new HashSet<>();
    private ImageDto image;
    private Integer sequence;
    private String username;

    public StudentQuestionDto() {}

    public StudentQuestionDto(StudentQuestion studentQuestion) {
        this.id = studentQuestion.getId();
        this.key = studentQuestion.getKey();
        this.title = studentQuestion.getTitle();
        this.content = studentQuestion.getContent();
        this.status = studentQuestion.getStatus().name();
        this.options = studentQuestion.getOptions().stream().map(OptionDto::new).collect(Collectors.toSet());
        this.username = studentQuestion.getStudent().getUsername();

        if (studentQuestion.getImage() != null)
            this.image = new ImageDto(studentQuestion.getImage());
        if (studentQuestion.getCreationDate() != null)
            this.creationDate = studentQuestion.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
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
        return LocalDateTime.parse(this.creationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
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

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getUsername() {
        return this.username;
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
                ", sequence=" + sequence +
                ", student=" + username +
                '}';
    }

}
