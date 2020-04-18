import Option from '@/models/management/Option';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';

export default class StudentQuestion {
  id: number | null = null;
  title: string = '';
  status: string = 'AVAILABLE';
  content: string = '';
  creationDate!: string | null;
  image: Image | null = null;
  sequence: number | null = null;
  creatorUsername!: number | null;
  lastReviewerUsername!: number | null;
  reviewedDate!: string | null;
  rejectedExplanation!: string | null;

  options: Option[] = [new Option(), new Option(), new Option(), new Option()];
  topics: Topic[] = [];

  constructor(jsonObj?: StudentQuestion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.status = jsonObj.status;
      this.content = jsonObj.content;
      this.image = jsonObj.image;
      this.creationDate = jsonObj.creationDate;

      this.creatorUsername = jsonObj.creatorUsername;
      this.lastReviewerUsername = jsonObj.lastReviewerUsername;
      this.reviewedDate = jsonObj.creationDate;
      this.rejectedExplanation = jsonObj.creationDate;

      this.options = jsonObj.options.map(
        (option: Option) => new Option(option)
      );

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
    }
  }
}
