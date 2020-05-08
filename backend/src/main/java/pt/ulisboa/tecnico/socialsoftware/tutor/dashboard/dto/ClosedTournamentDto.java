package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.SolvedQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.io.Serializable;

public class ClosedTournamentDto implements Serializable {
    private Integer id;
    private String title;
    private UserDto creator;
    private Integer ranking;
    private String conclusionDate;
    private SolvedQuizDto solvedQuiz;

    public ClosedTournamentDto() {}

    public ClosedTournamentDto(QuizAnswer answer, Integer ranking) {
        Tournament tourn = answer.getQuiz().getTournament();
        id = tourn.getId();
        title = tourn.getTitle();
        creator = new UserDto(tourn.getCreator());
        conclusionDate = DateHandler.toISOString(tourn.getConclusionDate());
        this.ranking = ranking;
        solvedQuiz = new SolvedQuizDto(answer);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserDto getCreator() {
        return creator;
    }

    public void setCreator(UserDto creator) {
        this.creator = creator;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public SolvedQuizDto getSolvedQuiz() {
        return solvedQuiz;
    }

    public void setSolvedQuiz(SolvedQuizDto solvedQuiz) {
        this.solvedQuiz = solvedQuiz;
    }

    public String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(String conclusionDate) {
        this.conclusionDate = conclusionDate;
    }
}
