export class Discussion {
    id!: number;
    userId!: number;
    questionId!: number | undefined;
    messageFromStudent!: string | undefined;
    teacherAnswer!: string | undefined;

    constructor(jsonObj?: Discussion) {
        if (jsonObj) {
            this.id = jsonObj.id;
            this.userId = jsonObj.userId;
            this.questionId = jsonObj.questionId;
            this.messageFromStudent = jsonObj.messageFromStudent;
            this.teacherAnswer = jsonObj.teacherAnswer;
        }
    }
}
