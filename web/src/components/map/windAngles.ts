const windAngles: {[key: string]: number} = {
    "R": 0,
    "ZR": 45,
    "Z": 90,
    "ZA": 135,
    "A": 180,
    "DA": 225,
    "D": 270,
    "DR": 315,
}

export function getAngleFromString(str: string): number {
    return windAngles[str] ?? 0;
}

export function drawRotatedImage(
    ctx: CanvasRenderingContext2D,
    img: HTMLImageElement,
    x: number,
    y: number,
    angleInDegrees: number,
): void {
    const angleInRadians = angleInDegrees * (Math.PI / 180);
    ctx.translate(x + img.width / 2, y + img.height / 2);
    ctx.rotate(angleInRadians);
    ctx.drawImage(img, -img.width / 2, -img.height / 2, img.width, img.height);
    ctx.setTransform(1, 0, 0, 1, 0, 0);
}
