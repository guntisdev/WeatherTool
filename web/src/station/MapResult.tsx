import { Accessor, Component, Setter, createEffect, createResource, createSignal, onMount } from "solid-js";

import { coordToCity } from "./coordToCity";
import { cityCoords } from "./cityCoords";
import mapUrl from "../assets/map_1000x570.webp";
import moment from "moment";
import { apiHost } from "../consts";

export const MapResult: Component<{
    setCity: Setter<string | undefined>,
    getCities: Accessor<Set<string>>,
    getField: Accessor<string>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
}> = (props) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());

    let ctx: CanvasRenderingContext2D | undefined;

    onMount(() => {
        const canvas = getCanvas()!;
        canvas.addEventListener("click", event => {
            const rect = canvas.getBoundingClientRect();
            const scaleX = canvas.width / rect.width;
            const scaleY = canvas.height / rect.height;

            const x = Math.round((event.clientX - rect.left) * scaleX);
            const y = Math.round((event.clientY - rect.top) * scaleY);

            const selectedCityCoords = Object.entries(cityCoords).filter(([city]) => props.getCities().has(city));
            const optionalCity = coordToCity(x, y, selectedCityCoords);
            props.setCity(optionalCity);
        });

        ctx = canvas.getContext("2d")!;

        const img = new Image();
        img.onload = () => setImg(img);
        img.src = mapUrl;
    });

    const citiesFieldDate = (): [Set<string>, string, Date, Date] => [
        props.getCities(),
        props.getField(),
        props.getStart(),
        props.getEnd(),
    ];

    const [meteoValues] = createResource(citiesFieldDate, async ([cities, field, start, end]: [Set<string>, string, Date, Date]) => {
        if (cities.size === 0) return;
        const queryStart = moment(start).format("YYYYMMDD_HHmm");
        const queryEnd = moment(end).format("YYYYMMDD_HHmm");
        let aggregate: string = "avg";
        if (["tempMax", "windMax"].includes(field)) aggregate = "max";
        if (["tempMin", "visibilityMin"].includes(field)) aggregate = "min";
        if (["precipitation", "sunDuration"].includes(field)) aggregate = "sum";
        const response = await fetch(`${apiHost}/api/query/city/${[...cities].join(",")}/${queryStart}-${queryEnd}/hour/${field}/${aggregate}`);
        const json = await response.json();
        return json.result;
    });

    createEffect(() => {
        if (!ctx || !getImg() || !meteoValues()) return;
        const values = meteoValues();
        if (!isValidResult(values)) return;

        const cityValues: [string, number | undefined][] = [...props.getCities()].map(city => [city, values[city]]);

        drawOnMap(
            ctx!,
            [getImg()],
            cityValues,
        );
    });

    return <canvas ref={setCanvas} width={1000} height={570} />;
}

function drawOnMap(
    ctx: CanvasRenderingContext2D,
    imgArr: [HTMLImageElement],
    cityValues: [string, number | undefined][],
): void {
    ctx.drawImage(imgArr[0], 0, 0);
    ctx.fillStyle = "red";
    ctx.font = "18px serif";
    cityValues.forEach(([city, optionalValue]) => {
        const coord = cityCoords[city];
        if (!coord) return;

        const value = optionalValue === undefined ? "" : optionalValue + "";

        ctx.beginPath();
        // ctx.arc(coord.x, coord.y, 4, 0, 2 * Math.PI);
        const cityTextSize = ctx.measureText(city);
        ctx.fillText(city, coord.x - cityTextSize.width/2, coord.y - 4);
        const valueTextSize = ctx.measureText(value);
        ctx.fillText(value, coord.x - valueTextSize.width/2, coord.y + 14);
        ctx.fill();
        // const boxWidth = 60;
        // const boxHeight = 30;
        // ctx.strokeRect(coord.x-boxWidth/2, coord.y-boxHeight/2, boxWidth, boxHeight);
    });
}

function isValidResult(obj: any): obj is { [key: string]: number } {
    if (typeof obj !== "object" || obj === null) {
        return false;
    }

    for (const key in obj) {
        if (typeof key !== "string") return false;
        if (typeof obj[key] !== "number") return false;
    }

    return true;
}