import { Component, createEffect, createSignal, onMount } from "solid-js";
import moment from "moment";

import { cityCoords } from "./cityCoords";
import { SelectCity } from "../components/SelectCity";

import mapUrl from "../assets/map_1000x570.webp";
import "../css/station.css"
import { SelectField } from "../components/SelectField";
import { coordToCity } from "./coordToCity";
import { Result } from "./Result";

export const Station: Component<{}> = () => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());
    const [getShowCities, setShowCities] = createSignal(false);
    const [getCities, setCities] = createSignal<Set<string>>(new Set(["Ainaži", "Rīga", "Rēzekne", "Liepāja", "Daugavpils", "Ventspils", "Madona"]));
    const [getDate, setDate] = createSignal(moment());
    const [getField, setField] = createSignal("tempMax");
    const [getCity, setCity] = createSignal<string | undefined>("Rīga");
    const [getStart, setStart] = createSignal(moment().subtract(1, "days").toDate());
    const [getEnd, setEnd] = createSignal(moment().toDate());

    let ctx: CanvasRenderingContext2D | undefined;

    onMount(() => {
        const canvas = getCanvas()!;
        canvas.addEventListener("click", event => {
            const rect = canvas.getBoundingClientRect();
            const scaleX = canvas.width / rect.width;
            const scaleY = canvas.height / rect.height;

            const x = Math.round((event.clientX - rect.left) * scaleX);
            const y = Math.round((event.clientY - rect.top) * scaleY);

            const selectedCityCoords = Object.entries(cityCoords).filter(([city]) => getCities().has(city));
            const optionalCity = coordToCity(x, y, selectedCityCoords);
            setCity(optionalCity);
        });

        ctx = canvas.getContext("2d")!;

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
                        <input type="date" value={getDate().format("YYYY-MM-DD")} />
                        <SelectField getField={getField} setField={setField} />
                    </div>
                    <div class={"cities " + (getShowCities() ? "visible" : "hidden")}>
                        <SelectCity getCities={getCities} setCities={setCities} />
                    </div>
                    <canvas ref={setCanvas} width={1000} height={570} />
                </div>
                <div class="column">
                    <Result
                        getCity={getCity}
                        getField={getField}
                        getStart={getStart}
                        getEnd={getEnd}
                    />
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
    ctx.drawImage(imgArr[0], 0, 0);
    ctx.fillStyle = "red";
    ctx.font = "18px serif";
    cities.forEach(city => {
        const coord = cityCoords[city];
        if (!coord) return;

        ctx.beginPath();
        ctx.arc(coord.x, coord.y, 4, 0, 2 * Math.PI);
        const cityTextSize = ctx.measureText(city);
        ctx.fillText(city, coord.x - cityTextSize.width/2, coord.y - 6);
        ctx.fill();
        // const boxWidth = 60;
        // const boxHeight = 30;
        // ctx.strokeRect(coord.x-boxWidth/2, coord.y-boxHeight/2, boxWidth, boxHeight);
    });
}