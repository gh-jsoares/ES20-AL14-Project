import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { Quiz } from '@/models/management/Quiz';

export class Tournament {
  id!: number;
  scramble!: boolean;
  title!: string;
  creationDate!: string | undefined;
  availableDate!: string | undefined;
  conclusionDate!: string | undefined;
  state!: string;
  series!: number;
  version!: string;
  numberOfQuestions!: number;
  numberOfEnrolls!: number;

  topics: Topic[] = [];
  creator: User | undefined;
  quiz: Quiz | undefined;

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
      this.creationDate = jsonObj.creationDate;
      this.availableDate = jsonObj.availableDate;
      this.conclusionDate = jsonObj.conclusionDate;
      this.creator = jsonObj.creator;
      this.quiz = jsonObj.quiz;

      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map(
(topic: Topic) => new Topic(topic));
      }
    }
  }
}
