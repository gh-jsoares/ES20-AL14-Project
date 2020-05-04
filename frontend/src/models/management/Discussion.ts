import Question from '@/models/management/Question';
import Message from '@/models/management/Message';

export class Discussion {
  id: number | null = null;
  question!: Question;
  messages: Message[] = [];

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.question = jsonObj.question;
      this.messages = jsonObj.messages.map(
        (message: Message) => new Message(message)
      );
    }
  }
}
