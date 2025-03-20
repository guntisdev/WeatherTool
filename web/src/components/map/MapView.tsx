import { Component, createEffect, createSignal, onMount } from "solid-js";

import { ResultKeyVal } from "../../consts";
import { WindInputs, WindSignals } from "./WindInputs";

import arrowUrl from "../../assets/arrow.webp";
import { cityCoords } from "../cityCoords";
import { MapResolution, resolutionProps, windProps, WindProps } from "./mapConsts";
import { CityData, drawOnMap } from "./canvasDraw";
import { IconInputs } from "../weatherIcons/IconInputs";

export const MapView: Component<{ type: MapResolution, data: () => ResultKeyVal[] }> = ({ type, data }) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getImg, setImg] = createSignal(new Image());
    const weatherIconsSingal = createSignal<{[key: string]: string;}>({});
    const [getWeatherIcons] = weatherIconsSingal;
    
    let lastCoords: CityData[] = [];
    const props = resolutionProps[type];

    const arrowImg = new Image();
    arrowImg.src = arrowUrl;

    const windSignals: WindSignals = {
        direction: createSignal(""),
        speed: createSignal(""),
        gusts: createSignal(""),
        roundValues: createSignal(false),
    }

    function getWindData(): [string, string, string, boolean, WindProps] {
        return [
            windSignals.direction[0](),
            windSignals.speed[0](),
            windSignals.gusts[0](),
            windSignals.roundValues[0](),
            windProps[type],
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
        const weatherIcons = getWeatherIcons();
        const coordsAndData: CityData[] = data()
            .map(([city, value]) => [
                city,
                cityCoords[city as keyof typeof cityCoords],
                typeof value === "number" ? value : -99,
                weatherIcons[city],
            ]);
            lastCoords = coordsAndData;
        drawOnMap(
            ctx,
            [getImg(), arrowImg],
            coordsAndData,
            getWindData(),
            props,
        );
    });

    const getStyleSize = () => {
        return props.width === 1920
            ? { "width": "1000px", "height": "562px" }
            : { "width": "1000px", "height": "374px" };
    }

    return (
        <>
            <div class="grid-1-2">
                <div>
                    <div>
                        <label>
                            <input
                                type="checkbox"
                                checked={windSignals.roundValues[0]()}
                                onChange={e => windSignals.roundValues[1](e.target.checked)}
                            />
                            Round values
                        </label>
                    </div>
                    { props.showWind && <WindInputs signals={windSignals} /> }
                </div>
                <div><IconInputs weatherIconsSingal={weatherIconsSingal} /></div>
            </div>
            <canvas
                ref={setCanvas}
                width={props.width+"px"}
                height={props.height+"px"}
                style={getStyleSize()}
            />
        </>
    );
}
