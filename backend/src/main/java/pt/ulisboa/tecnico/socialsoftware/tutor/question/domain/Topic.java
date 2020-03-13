package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament;

import javax.persistence.*;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_TOPIC_ALREADY_ADDED;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_TOPIC_NOT_PRESENT;

@Entity
@Table(name = "topics")
public class Topic {

    @SuppressWarnings("unused")
    public enum Status {
        DISABLED, REMOVED, AVAILABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToMany
    private Set<Question> questions = new HashSet<>();

    @ManyToOne
    private Topic parentTopic;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parentTopic", fetch=FetchType.EAGER)
    private Set<Topic> childrenTopics = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    private List<TopicConjunction> topicConjunctions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToMany
    private Set<StudentQuestion> studentQuestions = new HashSet<>();

    @ManyToMany(fetch=FetchType.LAZY)
    private Set<Tournament> tournaments = new HashSet<>();

    public Topic() {
    }

    public Topic(Course course, TopicDto topicDto) {
        this.name = topicDto.getName();
        this.course = course;
        course.addTopic(this);
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

    public Set<Question> getQuestions() {
        return questions;
    }

    public Topic getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(Topic parentTopic) {
        this.parentTopic = parentTopic;
    }

    public Set<Topic> getChildrenTopics() {
        return childrenTopics;
    }

    public List<TopicConjunction> getTopicConjunctions() {
        return topicConjunctions;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void addTopicConjunction(TopicConjunction topicConjunction) {
        this.topicConjunctions.add(topicConjunction);
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public Set<StudentQuestion> getStudentQuestions() {
        return studentQuestions;
    }

    public void addStudentQuestion(StudentQuestion studentQuestion) {
        checkDuplicateStudentQuestion(studentQuestion);

        this.studentQuestions.add(studentQuestion);
    }
    public void removeStudentQuestion(StudentQuestion studentQuestion) {
        checkStudentQuestionPresent(studentQuestion);
        this.studentQuestions.remove(studentQuestion);
    }

    private void checkStudentQuestionPresent(StudentQuestion studentQuestion) {
        if(getStudentQuestions().stream().noneMatch(sq -> sq.getKey().equals(studentQuestion.getKey())))
            throw new TutorException(STUDENT_QUESTION_TOPIC_NOT_PRESENT);
    }

    private void checkDuplicateStudentQuestion(StudentQuestion studentQuestion) {
        if (getStudentQuestions().stream().anyMatch(sq -> sq.getKey().equals(studentQuestion.getKey())))
            throw new TutorException(STUDENT_QUESTION_TOPIC_ALREADY_ADDED);
    }

    public Set<Tournament> getTournaments() {
        return tournaments;
    }

    public void addTournament(Tournament tournament) {
        this.tournaments.add(tournament);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return name.equals(topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentTopic=" + parentTopic +
                '}';
    }

    public void remove() {
        getCourse().getTopics().remove(this);
        course = null;

        getQuestions().forEach(question -> question.getTopics().remove(this));
        getQuestions().clear();


        getStudentQuestions().forEach(studentQuestion  -> studentQuestion.getTopics().remove(this));
        getStudentQuestions().clear();

        if (this.parentTopic != null) {
            parentTopic.getChildrenTopics().remove(this);
            parentTopic.getChildrenTopics().addAll(this.getChildrenTopics());
        }

        this.childrenTopics.forEach(topic -> topic.parentTopic = this.parentTopic);
        this.topicConjunctions.forEach(topicConjunction -> topicConjunction.getTopics().remove(this));

        this.parentTopic = null;
        this.childrenTopics.clear();
    }
}
