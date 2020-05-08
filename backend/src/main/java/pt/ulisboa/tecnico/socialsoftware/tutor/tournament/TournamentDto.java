package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TournamentDto implements Serializable {
    private Integer id;
    private Tournament.State state;
    private String title;
    private String creationDate = null;
    private String availableDate = null;
    private String conclusionDate = null;
    private boolean scramble;
    private UserDto creator;
    private boolean userEnrolled = false;
    private Integer numberOfEnrolls;
    private List<TopicDto> topics = new ArrayList<>();
    private StatementQuizDto statementQuiz;
    private Integer numberOfQuestions;
    private Integer series;
    private String version;

    public TournamentDto(){

    }

    public TournamentDto(Tournament tournament){
        setVariables(tournament);
    }

    public TournamentDto(Tournament tournament, int userId){
        setVariables(tournament);
        setUserEnrolled(tournament.isStudentEnrolled(userId));
    }

    private void setVariables(Tournament tournament) {
        setId(tournament.getId());
        setTitle(tournament.getTitle());
        setNumberOfQuestions(tournament.getNumberOfQuestions());
        setState(tournament.getState());
        setScramble(tournament.isScramble());
        setNumberOfEnrolls(tournament.getEnrolledStudents().size());
        setCreationDate(DateHandler.toISOString(tournament.getCreationDate()));
        setAvailableDate(DateHandler.toISOString(tournament.getAvailableDate()));
        setConclusionDate(DateHandler.toISOString(tournament.getConclusionDate()));
        setSeries(tournament.getSeries());
        setVersion(tournament.getVersion());
        if (tournament.getCreator() != null) {
            setCreator(new UserDto(tournament.getCreator()));
        }
        for (Topic t : tournament.getTopics()) {
            addTopic(new TopicDto(t));
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(String availableDate) {
        this.availableDate = availableDate;
    }

    public String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(String conclusionDate) {
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

    public StatementQuizDto getStatementQuiz() {
        return statementQuiz;
    }

    public void setStatementQuiz(StatementQuizDto statementQuiz) {
        this.statementQuiz = statementQuiz;
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

    public boolean isUserEnrolled() {
        return userEnrolled;
    }

    public void setUserEnrolled(boolean enrolled) {
        userEnrolled = enrolled;
    }
}
