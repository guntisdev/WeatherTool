import { ResolutionPropsValue, WindProps, windProps } from "./mapConsts";
import { drawRotatedImage, getAngleFromString } from "./windAngles";

export function drawOnMap(
    ctx: CanvasRenderingContext2D,
    imgArr: [HTMLImageElement, HTMLImageElement],
    coords: [string, {x: number, y: number }, number][],
    windData: [string, string, string, boolean, WindProps],
    props: ResolutionPropsValue,
): void {
    const boxSize = 80;

    const [windDirection, windSpeed, windGusts, roundValues, windProp] = windData;
    const [bgImg, arrowImg] = imgArr;

    // bg
    ctx.drawImage(bgImg, 0, 0);

    // city boxes
    ctx.fillStyle = "#FFFFFF";
    coords.forEach(([, {x, y}, value]) => {
        const localX = props.offsetX + x * props.scale;
        const localY = props.offsetY + y * props.scale;

        ctx.beginPath();
        ctx.fillStyle = "#FFFFFF";
        ctx.rect(localX-boxSize/2, localY-boxSize/2, boxSize, boxSize);
        ctx.fill();
    });
    ctx.fill();

    // weather values
    ctx.font = "bold 30px Rubik";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    coords.forEach(([, {x, y}, value]) => {
        const localX = props.offsetX + x * props.scale;
        const localY = props.offsetY + y * props.scale;

        ctx.fillStyle = "#000000";
        const numericValue = roundValues ? Math.round(value) : value;
        const stringValue = numericValue.toString().replace(".", ",");
        ctx.fillText(stringValue, localX, localY);
    });

    if (!props.showWind) return;

    // wind values
    ctx.font = "bold 45px Rubik";
    ctx.textAlign = "left";
    ctx.textBaseline = "top";
    ctx.fillStyle = "#FFFFFF";
    const boxMiddleX = 1675;

    const directionWidth = ctx.measureText(windDirection).width;
    ctx.fillText(windDirection, windProp.offsetX + boxMiddleX - directionWidth / 2 + 30, (windProp.offsetY + 370)*windProp.scaleY);
    const angleInDegrees = getAngleFromString(windDirection);
    drawRotatedImage(ctx, arrowImg, windProp.offsetX + boxMiddleX - directionWidth / 2 - 30, (windProp.offsetY + 370)*windProp.scaleY, angleInDegrees);

    const speedWidth = ctx.measureText(windSpeed).width;
    ctx.fillText(windSpeed, windProp.offsetX + boxMiddleX - speedWidth / 2 - 30, (windProp.offsetY + 575)*windProp.scaleY);
    ctx.font = "bold 25px Rubik";
    ctx.fillText("M/S", windProp.offsetX + boxMiddleX + speedWidth / 2 - 22, (windProp.offsetY + 590)*windProp.scaleY);

    ctx.font = "bold 45px Rubik";
    const gustsWidth = ctx.measureText(windGusts).width;
    ctx.fillText(windGusts, windProp.offsetX + boxMiddleX - gustsWidth / 2 - 30, (windProp.offsetY + 790)*windProp.scaleY);
    ctx.font = "bold 25px Rubik";
    ctx.fillText("M/S", windProp.offsetX + boxMiddleX + gustsWidth / 2 - 22, (windProp.offsetY + 805)*windProp.scaleY);
}
