package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Tournament {
    public enum State {ENROLL, ONGOING, CLOSED}

    private Integer id;

    private Integer key;

    private State state;

    private String title = "Title";

    private LocalDateTime creationDate;

    private LocalDateTime availableDate;

    private LocalDateTime conclusionDate;

    private boolean scramble = false;

    private User creator;

    private Set<User> enrolledStudents = new HashSet<>();

    private Set<Topic> topics = new HashSet<>();

    private Quiz quiz;

    private Integer numberOfQuestions;

    private CourseExecution courseExecution;

    private Integer series;
    private String version;

    public Tournament(){

    }

    public Tournament(TournamentDto tournamentDto){
        setKey(tournamentDto.getKey());
        setTitle(tournamentDto.getTitle());
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        setState(tournamentDto.getState());
        setScramble(tournamentDto.isScramble());
        setCreationDate(tournamentDto.getCreationDate());
        setAvailableDate(tournamentDto.getAvailableDate());
        setConclusionDate(tournamentDto.getConclusionDate());
        setSeries(tournamentDto.getSeries());
        setVersion(tournamentDto.getVersion());
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        this.availableDate = availableDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public boolean isScramble() {
        return scramble;
    }

    public void setScramble(boolean scramble) {
        this.scramble = scramble;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void addEnrolledStudent(User user) {
        this.enrolledStudents.add(user);
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
