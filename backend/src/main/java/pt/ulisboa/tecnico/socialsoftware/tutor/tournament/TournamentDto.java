package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TournamentDto implements Serializable {
    private Integer id;
    private Integer key;
    private Tournament.State state;
    private String title;
    private LocalDateTime creationDate = null;
    private LocalDateTime availableDate = null;
    private LocalDateTime conclusionDate = null;
    private boolean scramble;
    private UserDto creator;
    private Integer numberOfEnrolls;
    private List<TopicDto> topics = new ArrayList<>();
    private QuizDto quiz;
    private Integer numberOfQuestions;
    private Integer series;
    private String version;

    public TournamentDto(){

    }

    public TournamentDto(Tournament tournament){
        setKey(tournament.getKey());
        setTitle(tournament.getTitle());
        setNumberOfQuestions(tournament.getNumberOfQuestions());
        setState(tournament.getState());
        setScramble(tournament.isScramble());
        setCreationDate(tournament.getCreationDate());
        setNumberOfEnrolls(tournament.getEnrolledStudents().size());
        setAvailableDate(tournament.getAvailableDate());
        setConclusionDate(tournament.getConclusionDate());
        setSeries(tournament.getSeries());
        setVersion(tournament.getVersion());
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

    public Tournament.State getState() {
        return state;
    }

    public void setState(Tournament.State state) {
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

    public UserDto getCreator() {
        return creator;
    }

    public void setCreator(UserDto creator) {
        this.creator = creator;
    }

    public Integer getNumberOfEnrolls() {
        return numberOfEnrolls;
    }

    public void setNumberOfEnrolls(Integer numberOfEnrolls) {
        this.numberOfEnrolls = numberOfEnrolls;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    public void addTopic(TopicDto topic) {
        this.topics.add(topic);
    }

    public QuizDto getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizDto quiz) {
        this.quiz = quiz;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
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
