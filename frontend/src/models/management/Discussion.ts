import Question from '@/models/management/Question';
import Message from '@/models/management/Message';

export class Discussion {
  id!: number | null;
  question!: Question;
  messages: Message[] = [];
  //messageFromStudent!: string | undefined;
  //teacherAnswer!: string | undefined;
  //studentName!: string | undefined;
  //teacherName!: string | undefined;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.question = jsonObj.question;
      this.messages = jsonObj.messages.map(
        (message: Message) => new Message(message)
      );
      //this.studentName = jsonObj.studentName;
      //this.teacherName = jsonObj.teacherName;
      //this.messageFromStudent = jsonObj.messageFromStudent;
      //this.teacherAnswer = jsonObj.teacherAnswer;
    }
  }
}
