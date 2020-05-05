export default class Message {
  message: string | null = null;
  userName: string | null = null;

  constructor(jsonObj?: Message) {
    if (jsonObj) {
      this.message = jsonObj.message;
      this.userName = jsonObj.userName;
    }
  }
}
