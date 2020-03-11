package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;


import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TopicDto implements Serializable {
    private Integer id;
    private String name;
    private String parentTopic;
    private Integer numberOfQuestions;
    private Set<StudentQuestionDto> studentQuestions = new HashSet<>();

    public TopicDto() {
    }

    public TopicDto(Topic topic) {
        this.id = topic.getId();
        this.name = topic.getName();
        if (topic.getParentTopic() != null) {
            this.parentTopic = topic.getParentTopic().getName();
        }
        this.numberOfQuestions = topic.getQuestions().size();

        this.studentQuestions = topic.getStudentQuestions().stream().map(StudentQuestionDto::new).collect(Collectors.toSet());
    }

    public TopicDto(TopicDto topicDto) {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(String parentTopic) {
        this.parentTopic = parentTopic;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public Set<StudentQuestionDto> getStudentQuestions() {
        return studentQuestions;
    }

    public void addStudentQuestion(StudentQuestionDto studentQuestionDto) {
        this.studentQuestions.add(studentQuestionDto);
    }

    @Override
    public String toString() {
        return "TopicDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentTopic='" + parentTopic + '\'' +
                '}';
    }

}
