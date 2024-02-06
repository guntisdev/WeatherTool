import { Component, createEffect, createSignal, onMount } from "solid-js";

import { ResultKeyVal } from "../../consts";
import { drawRotatedImage, getAngleFromString } from "./windAngles";
import { WindInputs, WindSignals } from "./WindInputs";

import map_1920x1080 from "../../assets/map_1920x1080.webp";
import map_1920x1080_wind from "../../assets/map_1920x1080_wind.webp";
import map_3840x1440 from "../../assets/map_3840x1440.webp";
import map_3840x1440_wind from "../../assets/map_3840x1440_wind.webp";
import arrowUrl from "../../assets/arrow.webp";
import { cityCoords } from "../../components/cityCoords";

type MapResolution = "map_1920x1080" | "map_1920x1080_wind" | "map_3840x1440" | "map_3840x1440_wind";

type ResolutionPropsValue = {
    offsetX: number;
    offsetY: number;
    width: number;
    height: number;
    scale: number;
    showWind: boolean;
    map: string;
};

const resolutionProps: Record<string, ResolutionPropsValue> = {
    "map_1920x1080":        { offsetX: 90, offsetY: 120, width: 1920, height: 1080, scale: 1.35, showWind: false, map: map_1920x1080 },
    "map_1920x1080_wind":   { offsetX: 90, offsetY: 120, width: 1920, height: 1080, scale: 1.35, showWind: true, map: map_1920x1080_wind },
    "map_3840x1440":        { offsetX: 800, offsetY: 120, width: 3840, height: 1440, scale: 2.2, showWind: false, map: map_3840x1440 },
    "map_3840x1440_wind":   { offsetX: 800, offsetY: 120, width: 3840, height: 1440, scale: 2.2, showWind: true, map: map_3840x1440_wind },
}

export const MapView: Component<{ type: MapResolution, data: () => ResultKeyVal[] }> = ({ type, data }) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());
    let lastCoords: [string, {x: number, y: number }, number][] = [];
    const props = resolutionProps[type];

    const arrowImg = new Image();
    arrowImg.src = arrowUrl;

    const windSignals: WindSignals = {
        direction: createSignal(""),
        speed: createSignal(""),
        gusts: createSignal(""),
        roundValues: createSignal(false),
    }

    function getWindData(): [string, string, string, boolean] {
        return [
            windSignals.direction[0](),
            windSignals.speed[0](),
            windSignals.gusts[0](),
            windSignals.roundValues[0](),
        ];
    }

    onMount(() => {
        const ctx = getCanvas()!.getContext("2d")!;
        const img = new Image();
        img.onload = () => {
            setImg(img);
            drawOnMap(
                ctx,
                [img, arrowImg],
                lastCoords,
                getWindData(),
                props,
            );
        };
        img.src = props.map;
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
        drawOnMap(
            ctx,
            [getImg(),arrowImg],
            coordsAndData,
            getWindData(),
            props,
        );
    });

    return (
        <>
            <WindInputs signals={windSignals} />
            <canvas
                ref={setCanvas}
                width={props.width+"px"}
                height={props.height+"px"}
                style={{"max-width": "1000px"}}
            />
        </>
    );
}

function drawOnMap(
    ctx: CanvasRenderingContext2D,
    imgArr: [HTMLImageElement, HTMLImageElement],
    coords: [string, {x: number, y: number }, number][],
    windData: [string, string, string, boolean],
    props: ResolutionPropsValue,
): void {
    const boxSize = 80;

    const [windDirection, windSpeed, windGusts, roundValues] = windData;
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
    ctx.fillText(windDirection, boxMiddleX - directionWidth / 2 + 30, 370);
    const angleInDegrees = getAngleFromString(windDirection);
    drawRotatedImage(ctx, arrowImg, boxMiddleX - directionWidth / 2 - 30, 370, angleInDegrees);

    const speedWidth = ctx.measureText(windSpeed).width;
    ctx.fillText(windSpeed, boxMiddleX - speedWidth / 2 - 30, 575);
    ctx.font = "bold 25px Rubik";
    ctx.fillText("M/S", boxMiddleX + speedWidth / 2 - 22, 590);

    ctx.font = "bold 45px Rubik";
    const gustsWidth = ctx.measureText(windGusts).width;
    ctx.fillText(windGusts, boxMiddleX - gustsWidth / 2 - 30, 790);
    ctx.font = "bold 25px Rubik";
    ctx.fillText("M/S", boxMiddleX + gustsWidth / 2 - 22, 805);
}