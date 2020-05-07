import User from '@/models/user/User';
import { ISOtoString } from '@/services/ConvertDateService';
import SolvedQuiz from '@/models/statement/SolvedQuiz';

export class ClosedTournament {
  id!: number;
  title!: string;
  creator!: User;
  ranking!: number;
  conclusionDate!: string;
  solvedQuiz!: SolvedQuiz;

  constructor(jsonObj?: ClosedTournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.ranking = jsonObj.ranking;
      this.conclusionDate = ISOtoString(jsonObj.conclusionDate);
      this.creator = jsonObj.creator;
      this.solvedQuiz = new SolvedQuiz(jsonObj.solvedQuiz);
    }
  }
}
