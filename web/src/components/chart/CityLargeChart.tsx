import { onMount, createSignal, Component } from "solid-js";
import { Chart } from "chart.js";

import "../../css/overlay.css"
import { createCustomChart } from "./CustomChart";
import { DataQuery, formatDateString } from "../../cities/helpers";

export const CityLargeChart: Component<{
    city: () => string;
    data: () => [string, number | null][];
    query: () => DataQuery;
    close: () => void
}> = (props) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    let chart: Chart;
    const data: [string, number | null][] = props.data().map(([dateStr, value]) => [
        formatDateString(dateStr),
        value,
    ]);

    // Split the data into two arrays for the x and y axis
    const timestamps = data.map(item => item[0]);
    const values = data.map(item => item[1]);
    
    onMount(() => {
        const ctx = getCanvas()!.getContext('2d')!;

        chart = createCustomChart(
            ctx,
            true,
            timestamps,
            values,
            props.city(),
            props.query().field,
            props.query().granularity,
        );
    });

    return (
        <div class="overlay" onClick={props.close}>
            <div class="overlay-content">
                <canvas ref={setCanvas} />
            </div>
        </div>
    );
}