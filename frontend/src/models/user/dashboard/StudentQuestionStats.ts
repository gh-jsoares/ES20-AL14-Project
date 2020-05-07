export default class StudentQuestionStats {
  userId!: number;
  studentName!: string;
  total!: number;
  approved!: number;
  rejected!: number;
  percentage!: number;
  visibilitySetting!: boolean;

  constructor(jsonObj?: StudentQuestionStats) {
    if (jsonObj) {
      this.total = jsonObj.total;
      this.approved = jsonObj.approved;
      this.rejected = jsonObj.rejected;
      this.percentage = jsonObj.percentage;
      this.userId = jsonObj.userId;
      this.studentName = jsonObj.studentName;
      this.visibilitySetting = jsonObj.visibilitySetting;
    }
  }
}
