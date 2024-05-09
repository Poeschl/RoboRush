export default class Color {
  r: number;
  g: number;
  b: number;

  constructor(r: number, g: number, b: number) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  enlightenWithPercentage(percentage: number): Color {
    const volumeRtoMax = 255 - this.r;
    const volumeGtoMax = 255 - this.g;
    const volumeBtoMax = 255 - this.b;

    return new Color(
      this.r + Math.floor(percentage * volumeRtoMax),
      this.g + Math.floor(percentage * volumeGtoMax),
      this.b + Math.floor(percentage * volumeBtoMax),
    );
  }

  toHex() {
    let r = this.r.toString(16);
    let g = this.g.toString(16);
    let b = this.b.toString(16);

    if (r.length == 1) r = "0" + r;
    if (g.length == 1) g = "0" + g;
    if (b.length == 1) b = "0" + b;

    return "#" + r + g + b;
  }

  getLightLevel() {
    return this.r + this.g + this.b;
  }
}
