import { Component, createEffect, createSignal, onMount } from "solid-js";

import mapUrl from "../assets/map_1000x570.webp";

import "../css/station.css"
import { cityCoords } from "./cityCoords";
import { SelectCity } from "../components/SelectCity";

export const Station: Component<{}> = () => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());
    const [getShowCities, setShowCities] = createSignal(false);
    const [getCities, setCities] = createSignal<Set<string>>(new Set(["Rīga", "Rēzekne", "Liepāja"]));

    let ctx: CanvasRenderingContext2D | undefined;

    onMount(() => {
        ctx = getCanvas()!.getContext("2d")!;

        const img = new Image();
        img.onload = () => setImg(img);
        img.src = mapUrl;
    });

    createEffect(() => {
        if (!ctx || !getImg()) return;
        drawOnMap(
            ctx!,
            [getImg()],
            getCities(),
        );
    });

    return (
        <div class="stationWrapper">
            <div class="container">
                <div class="column">
                    <div class="submenu">
                        <input
                            type="button"
                            value="Select stations"
                            onClick={() => setShowCities(!getShowCities())}
                        />
                    </div>
                    <div class={"cities " + (getShowCities() ? "visible" : "hidden")}>
                        <SelectCity getCities={getCities} setCities={setCities} />
                    </div>
                    <canvas ref={setCanvas} width={1000} height={570} />
                </div>
                <div class="column">
                    3rd
                </div>
            </div>
        </div>
    );
}

function drawOnMap(
    ctx: CanvasRenderingContext2D,
    imgArr: [HTMLImageElement],
    cities: Set<string>,
): void {
    console.log("Draw cities");
    ctx.drawImage(imgArr[0], 0, 0);
    ctx.fillStyle = "red";
    ctx.font = "18px serif";
    cities.forEach(city => {
        const coord = cityCoords[city];
        if (!coord) return;

        ctx.beginPath();
        ctx.arc(coord.x, coord.y, 6, 0, 2 * Math.PI);
        const cityTextSize = ctx.measureText(city);
        ctx.fillText(city, coord.x - cityTextSize.width/2, coord.y - 6);
        ctx.fill();
    });
}