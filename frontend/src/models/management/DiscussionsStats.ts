export class DiscussionsStats {
    discussionsNumber!: number;
    publicDiscussionsNumber!: number;

    constructor(jsonObj?: DiscussionsStats) {
        if (jsonObj) {
            this.discussionsNumber = jsonObj.discussionsNumber;
            this.publicDiscussionsNumber = jsonObj.publicDiscussionsNumber;
        }
    }
}