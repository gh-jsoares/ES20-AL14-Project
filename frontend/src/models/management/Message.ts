export default class Message {
  message!: string;
  userName: string | null = null;

  constructor(jsonObj?: Message) {
    if (jsonObj) {
      this.message = jsonObj.message;
      this.userName = jsonObj.userName;
    }
  }
}
