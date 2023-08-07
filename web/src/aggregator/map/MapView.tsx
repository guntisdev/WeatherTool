import { Component, createEffect, createSignal, onMount } from "solid-js";
import { ResultKeyVal } from "../../consts";
import { cityCoords } from "./cityCoords";

export const MapView: Component<{ data: () => ResultKeyVal[] }> = ({ data }) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());
    let lastCoords: [string, {x: number, y: number }, number][] = [];

    onMount(() => {
        const ctx = getCanvas()!.getContext("2d")!;
        const img = new Image();
        img.onload = () => {
            setImg(img);
            drawOnMap(ctx, img, lastCoords);
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
        drawOnMap(ctx, getImg(), coordsAndData);
    });

    return (
        <canvas ref={setCanvas} width="1920px" height="1080px" />
    );
}

function drawOnMap(
    ctx: CanvasRenderingContext2D,
    img: HTMLImageElement,
    coords: [string, {x: number, y: number }, number][]
): void {
    const size = 80;
    ctx.font = "bold 30px Arial";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";

    ctx.drawImage(img, 0, 0);
    coords.forEach(([, {x, y}, value]) => {
        ctx.beginPath();
        ctx.fillStyle = "#FFFFFF";
        ctx.rect(x, y, size, size);
        ctx.fill();
        ctx.fillStyle = "#000000";
        ctx.fillText(`${value}`, x + size/2, y + size/2);
    });
}