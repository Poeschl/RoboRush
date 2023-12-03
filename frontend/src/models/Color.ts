export default class Color {
  r: number;
  g: number;
  b: number;

  constructor(r: number, g: number, b: number) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  enlighten(amount: number): Color {
    return new Color(Math.min(this.r + amount, 255), Math.min(this.g + amount, 255), Math.min(this.b + amount, 255));
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
}
