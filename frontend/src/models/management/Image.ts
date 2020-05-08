export default class Image {
  url!: string;
  width: number | null = null;

  constructor(jsonObj?: Image | null, url?: string) {
    if (jsonObj) {
      this.url = jsonObj.url;
      this.width = jsonObj.width;
    }
    if (url) this.url = url;
  }
}
