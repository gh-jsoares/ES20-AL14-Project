package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.StudentDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentQuestionDto implements Serializable {
    private Integer id;
    private Integer key;
    private String title;
    private String content;
    private int numberOfAnswers;
    private int numberOfCorrect;
    private String creationDate = null;
    private String status;
    private Set<OptionDto> options = new HashSet<>();
    private ImageDto image;
    private Integer sequence;
    private StudentDto student;

    public StudentQuestionDto() {}

    public StudentQuestionDto(StudentQuestion studentQuestion) {}

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

    public int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(int numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public int getNumberOfCorrect() {
        return numberOfCorrect;
    }

    public void setNumberOfCorrect(int numberOfCorrect) {
        this.numberOfCorrect = numberOfCorrect;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
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

    public StudentDto getStudent() {
        return student;
    }

    public void setStudent(StudentDto student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "StudentQuestionDto{" +
                "id=" + id +
                ", key=" + key +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", numberOfAnswers=" + numberOfAnswers +
                ", numberOfCorrect=" + numberOfCorrect +
                ", status='" + status + '\'' +
                ", options=" + options +
                ", image=" + image +
                ", sequence=" + sequence +
                ", student=" + student +
                '}';
    }
}
