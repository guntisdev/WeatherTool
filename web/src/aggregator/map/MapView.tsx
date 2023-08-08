import { Component, createEffect, createSignal, onMount } from "solid-js";

import { ResultKeyVal } from "../../consts";
import { cityCoords } from "./cityCoords";
import { drawRotatedImage, getAngleFromString } from "./windAngles";
import { WindInputs, WindSignals } from "./WindInputs";

const arrowImg = new Image();
arrowImg.src = "src/assets/arrow.webp";

export const MapView: Component<{ data: () => ResultKeyVal[] }> = ({ data }) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());
    let lastCoords: [string, {x: number, y: number }, number][] = [];

    const windSignals: WindSignals = {
        direction: createSignal(""),
        speed: createSignal(""),
        gusts: createSignal(""),
    }

    function getWindData(): [string, string, string] {
        return [
            windSignals.direction[0](),
            windSignals.speed[0](),
            windSignals.gusts[0](),
        ];
    }

    onMount(() => {
        const ctx = getCanvas()!.getContext("2d")!;
        const img = new Image();
        img.onload = () => {
            setImg(img);
            drawOnMap(ctx, img, lastCoords, getWindData());
        };
        img.src = "src/assets/map_1920x1080.webp";
    });

    createEffect(() => {
        const ctx = getCanvas()!.getContext("2d")!;
        const coordsAndData: [string, {x: number, y: number }, number][] = data()
            .map(([city, value]) => [
                city,
                cityCoords[city as keyof typeof cityCoords],
                typeof value === "number" ? value : -99,
            ]);
            lastCoords = coordsAndData;
        drawOnMap(ctx, getImg(), coordsAndData, getWindData());
    });

    return (
        <>
            <WindInputs signals={windSignals} />
            <canvas ref={setCanvas} width="1920px" height="1080px" />
        </>
    );
}

function drawOnMap(
    ctx: CanvasRenderingContext2D,
    img: HTMLImageElement,
    coords: [string, {x: number, y: number }, number][],
    windData: [string, string, string],
): void {
    const size = 80;

    // bg
    ctx.drawImage(img, 0, 0);

    // city boxes
    ctx.fillStyle = "#FFFFFF";
    coords.forEach(([, {x, y}, value]) => {
        ctx.rect(x, y, size, size);
    });
    ctx.fill();

    // weather values
    ctx.font = "bold 30px Arial";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillStyle = "#000000";
    coords.forEach(([, {x, y}, value]) => {
        ctx.rect(x, y, size, size);
        ctx.fillText(`${value}`, x + size/2, y + size/2);
    });

    // wind values
    ctx.font = "bold 45px Arial";
    ctx.textAlign = "left";
    ctx.textBaseline = "top";
    ctx.fillStyle = "#FFFFFF";
    const boxMiddleX = 1675;

    const directionWidth = ctx.measureText(windData[0]).width;
    ctx.fillText(windData[0], boxMiddleX - directionWidth / 2 + 30, 370);
    const angleInDegrees = getAngleFromString(windData[0]);
    drawRotatedImage(ctx, arrowImg, boxMiddleX - directionWidth / 2 - 30, 370, angleInDegrees);

    const speedWidth = ctx.measureText(windData[1]).width;
    ctx.fillText(windData[1], boxMiddleX - speedWidth / 2 - 30, 575);
    ctx.font = "bold 25px Arial";
    ctx.fillText("M/S", boxMiddleX + speedWidth / 2 - 22, 590);

    ctx.font = "bold 45px Arial";
    const gustsWidth = ctx.measureText(windData[2]).width;
    ctx.fillText(windData[2], boxMiddleX - gustsWidth / 2 - 30, 790);
    ctx.font = "bold 25px Arial";
    ctx.fillText("M/S", boxMiddleX + gustsWidth / 2 - 22, 805);
}