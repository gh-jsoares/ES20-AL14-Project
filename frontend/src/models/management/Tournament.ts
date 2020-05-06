import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { Quiz } from '@/models/management/Quiz';
import { ISOtoString } from '@/services/ConvertDateService';
import { QuizAnswer } from '@/models/management/QuizAnswer';
import StatementAnswer from '@/models/statement/StatementAnswer';
import StatementQuiz from '@/models/statement/StatementQuiz';

export class Tournament {
  id!: number;
  scramble!: boolean;
  title!: string;
  creationDate!: string | undefined;
  availableDate!: string;
  conclusionDate!: string;
  state!: string;
  series!: number;
  version!: string;
  numberOfQuestions!: number;
  numberOfEnrolls!: number;
  userEnrolled: boolean | undefined;

  topics: Topic[] = [];
  creator: User | undefined;
  statementQuiz: StatementQuiz | undefined;

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.scramble = jsonObj.scramble;
      this.title = jsonObj.title;
      this.state = jsonObj.state;
      this.series = jsonObj.series;
      this.version = jsonObj.version;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.numberOfEnrolls = jsonObj.numberOfEnrolls;
      if (!!jsonObj.creationDate)
        this.creationDate = ISOtoString(jsonObj.creationDate);
      this.availableDate = ISOtoString(jsonObj.availableDate);
      this.conclusionDate = ISOtoString(jsonObj.conclusionDate);
      this.userEnrolled = jsonObj.userEnrolled;
      this.creator = jsonObj.creator;
      this.statementQuiz = jsonObj.statementQuiz;

      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
      }
    }
  }
}
