import Question from '@/models/management/Question';

export class Discussion {
  id!: number | null;
  question!: Question;
  messageFromStudent!: string | undefined;
  teacherAnswer!: string | undefined;
  studentName!: string | undefined;
  teacherName!: string | undefined;
  visibleToOtherStudents!: boolean | undefined;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.studentName = jsonObj.studentName;
      this.teacherName = jsonObj.teacherName;
      this.question = jsonObj.question;
      this.messageFromStudent = jsonObj.messageFromStudent;
      this.teacherAnswer = jsonObj.teacherAnswer;
      this.visibleToOtherStudents = jsonObj.visibleToOtherStudents;
    }
  }
}
