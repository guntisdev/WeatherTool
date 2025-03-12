import { ResolutionPropsValue, WindProps, windProps } from "./mapConsts";
import { drawRotatedImage, getAngleFromString } from "./windAngles";

// cityName, {x, y}, weatherValue, icon
export type CityData = [cityName: string, {x: number, y: number }, weatherValue: number, icon: string | undefined]

export function drawOnMap(
    ctx: CanvasRenderingContext2D,
    imgArr: [HTMLImageElement, HTMLImageElement],
    cityData: CityData[],
    windData: [string, string, string, boolean, WindProps],
    props: ResolutionPropsValue,
): void {
    const [windDirection, windSpeed, windGusts, roundValues, windProp] = windData;
    const [bgImg, arrowImg] = imgArr;

    // bg
    ctx.drawImage(bgImg, 0, 0);

    // city boxes
    ctx.fillStyle = "#FFFFFF";
    cityData.forEach(([, {x, y}]) => {
        const localX = props.offsetX + x * props.scale;
        const localY = props.offsetY + y * props.scale;

        ctx.beginPath();
        ctx.fillStyle = "#FFFFFF";
        ctx.rect(localX-props.boxSize/2, localY-props.boxSize/2, props.boxSize, props.boxSize);
        ctx.fill();
    });
    ctx.fill();

    // weather values
    cityData.forEach(([, {x, y}, value, icon]) => {
        const localX = props.offsetX + x * props.scale;
        const localY = props.offsetY + y * props.scale;

        ctx.font = `bold ${props.fontSize}px Rubik`;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillStyle = "#000000";
        const numericValue = roundValues ? Math.round(value) : value;
        const stringValue = numericValue.toString().replace(".", ",");
        ctx.fillText(stringValue, localX, localY);

        if (icon) {
            ctx.fillStyle = "#FFFFFF";
            ctx.font = `normal ${props.fontSize * 5}px Daira by LTV grafika`;
            const iconSize = ctx.measureText(icon);
            ctx.fillText(icon, localX + props.boxSize/2 + 10 + iconSize.width/2, localY);
        }
    });

    if (!props.showWind) return;

    // wind values
    ctx.font = `bold ${props.windFontSize}px Rubik`;
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

    ctx.font = `bold ${props.windFontSize}px Rubik`;
    const gustsWidth = ctx.measureText(windGusts).width;
    ctx.fillText(windGusts, windProp.offsetX + boxMiddleX - gustsWidth / 2 - 30, (windProp.offsetY + 790)*windProp.scaleY);
    ctx.font = "bold 25px Rubik";
    ctx.fillText("M/S", windProp.offsetX + boxMiddleX + gustsWidth / 2 - 22, (windProp.offsetY + 805)*windProp.scaleY);
}
