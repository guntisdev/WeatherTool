import { Component, createEffect, createSignal, onMount } from "solid-js";

import { ResultKeyVal } from "../../consts";
import { WindInputs, WindSignals } from "./WindInputs";

import arrowUrl from "../../assets/arrow.webp";
import { cityCoords } from "../../components/cityCoords";
import { MapResolution, resolutionProps, ResolutionPropsValue, windProps, WindProps } from "./mapConsts";
import { drawOnMap } from "./canvasDraw";

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

    const getStyleSize = () => {
        return props.width === 1920
            ? { "width": "1000px", "height": "562px" }
            : { "width": "1000px", "height": "374px" };
    }

    return (
        <>
            { props.showWind && <WindInputs signals={windSignals} /> }
            <canvas
                ref={setCanvas}
                width={props.width+"px"}
                height={props.height+"px"}
                style={getStyleSize()}
            />
        </>
    );
}
