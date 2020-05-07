package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TournamentDashDto implements Serializable {
    private Integer totalTournaments = 0;
    private Integer totalFirstPlace = 0;
    private Integer totalSecondPlace = 0;
    private Integer totalThirdPlace = 0;
    private Integer totalUnrankedPlace = 0;
    private Integer totalSolved = 0;
    private Integer totalUnsolved = 0;
    private Integer totalPerfect = 0;
    private Integer totalCorrectAnswers = 0;
    private Integer totalWrongAnswers = 0;
    private double score = 0;
    private List<ClosedTournamentDto> closedTournaments = new ArrayList<>();
    private boolean anonimize = false;

    public Integer getTotalTournaments() {
        return totalTournaments;
    }

    public void setTotalTournaments(Integer totalTournaments) {
        this.totalTournaments = totalTournaments;
    }

    public Integer getTotalFirstPlace() {
        return totalFirstPlace;
    }

    public void setTotalFirstPlace(Integer totalFirstPlace) {
        this.totalFirstPlace = totalFirstPlace;
    }

    public Integer getTotalSecondPlace() {
        return totalSecondPlace;
    }

    public void setTotalSecondPlace(Integer totalSecondPlace) {
        this.totalSecondPlace = totalSecondPlace;
    }

    public Integer getTotalThirdPlace() {
        return totalThirdPlace;
    }

    public void setTotalThirdPlace(Integer totalThirdPlace) {
        this.totalThirdPlace = totalThirdPlace;
    }

    public Integer getTotalUnrankedPlace() {
        return totalUnrankedPlace;
    }

    public void setTotalUnrankedPlace(Integer totalUnrankedPlace) {
        this.totalUnrankedPlace = totalUnrankedPlace;
    }

    public Integer getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(Integer totalSolved) {
        this.totalSolved = totalSolved;
    }

    public Integer getTotalUnsolved() {
        return totalUnsolved;
    }

    public void setTotalUnsolved(Integer totalUnsolved) {
        this.totalUnsolved = totalUnsolved;
    }

    public Integer getTotalPerfect() {
        return totalPerfect;
    }

    public void setTotalPerfect(Integer totalPerfect) {
        this.totalPerfect = totalPerfect;
    }

    public Integer getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(Integer totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public Integer getTotalWrongAnswers() {
        return totalWrongAnswers;
    }

    public void setTotalWrongAnswers(Integer totalWrongAnswers) {
        this.totalWrongAnswers = totalWrongAnswers;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<ClosedTournamentDto> getClosedTournaments() {
        return closedTournaments;
    }

    public void setClosedTournaments(List<ClosedTournamentDto> closedTournaments) {
        this.closedTournaments = closedTournaments;
    }

    public boolean isAnonimize() { return anonimize; }

    public void setAnonimize(boolean anonimize) { this.anonimize = anonimize; }

    @Override
    public String toString() {
        return "TournamentStatsDto{" +
                "totalTournaments=" + totalTournaments +
                ", totalFirstPlace=" + totalFirstPlace +
                ", totalSecondPlace=" + totalSecondPlace +
                ", totalThirdPlace=" + totalThirdPlace +
                ", totalUnrankedPlace=" + totalUnrankedPlace +
                ", totalSolved=" + totalSolved +
                ", totalUnsolved=" + totalUnsolved +
                ", totalPerfect=" + totalPerfect +
                ", totalCorrectAnswers=" + totalCorrectAnswers +
                ", totalWrongAnswers=" + totalWrongAnswers +
                ", score=" + score +
                ", closedTournaments=" + closedTournaments +
                '}';
    }

}
