export class DiscussionsStats {
  discussionsNumber!: number;
  publicDiscussionsNumber!: number;
  areDiscussionsPublic!: boolean;

  constructor(jsonObj?: DiscussionsStats) {
    if (jsonObj) {
      this.discussionsNumber = jsonObj.discussionsNumber;
      this.publicDiscussionsNumber = jsonObj.publicDiscussionsNumber;
      this.areDiscussionsPublic = jsonObj.areDiscussionsPublic;
    }
  }
}
