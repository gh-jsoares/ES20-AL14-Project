import { ClosedTournament } from '@/models/management/ClosedTournament';

export class TournamentDashStats {
  totalTournaments!: number;
  totalFirstPlace!: number;
  totalSecondPlace!: number;
  totalThirdPlace!: number;
  totalUnrankedPlace!: number;
  totalSolved!: number;
  totalUnsolved!: number;
  totalPerfect!: number;
  totalCorrectAnswers!: number;
  totalWrongAnswers!: number;
  score!: number;
  closedTournaments: ClosedTournament[] = [];
  anonimize!: boolean;

  constructor(jsonObj?: TournamentDashStats) {
    if (jsonObj) {
      this.totalTournaments = jsonObj.totalTournaments;
      this.totalFirstPlace = jsonObj.totalFirstPlace;
      this.totalSecondPlace = jsonObj.totalSecondPlace;
      this.totalThirdPlace = jsonObj.totalThirdPlace;
      this.totalUnrankedPlace = jsonObj.totalUnrankedPlace;
      this.totalSolved = jsonObj.totalSolved;
      this.totalUnsolved = jsonObj.totalUnsolved;
      this.totalPerfect = jsonObj.totalPerfect;
      this.totalCorrectAnswers = jsonObj.totalCorrectAnswers;
      this.totalWrongAnswers = jsonObj.totalWrongAnswers;
      this.score = jsonObj.score;
      this.anonimize = jsonObj.anonimize;

      if (jsonObj.closedTournaments) {
        this.closedTournaments = jsonObj.closedTournaments.map(
          (tourn: ClosedTournament) => new ClosedTournament(tourn)
        );
      }
    }
  }
}
