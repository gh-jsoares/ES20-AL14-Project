import Question from '@/models/management/Question';
import Message from '@/models/management/Message';

export class Discussion {
  id!: number;
  question!: Question;
  messages: Message[] = [];
  visibleToOtherStudents!: boolean;
  needsAnswer!: boolean;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.question = jsonObj.question;
      this.messages = jsonObj.messages.map(
        (message: Message) => new Message(message)
      );
      this.visibleToOtherStudents = jsonObj.visibleToOtherStudents;
      this.needsAnswer = jsonObj.needsAnswer;
    }
  }
}
