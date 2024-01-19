import { onMount, onCleanup, createSignal, Component } from "solid-js";
import { Chart } from "chart.js";

import { CityLargeChart } from "./CityLargeChart";
import { createCustomChart } from "./CustomChart";
import { DataQuery, formatDateString } from "../../cities/helpers";

export const CityChart: Component<{
    city: () => string;
    data: () => [string, number | null][];
    query: () => DataQuery;
}> = (props) => {
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>();
    const [getIsLarge, setIsLarge] = createSignal(false);

    console.log("props.data()", props.data());

    let chart: Chart | undefined;
    const data: [string, number | null][] = props.data().map(([dateStr, value]) => [
        formatDateString(dateStr),
        value,
    ]);

    // Split the data into two arrays for the x and y axis
    const timestamps = data.map(item => item[0]);
    const values = data.map(item => item[1]);
    
    onMount(() => {
        const ctx = getCanvas()!.getContext("2d")!;
        chart = createCustomChart(
            ctx,
            false,
            timestamps,
            values,
            props.city(),
            props.query().field,
            props.query().granularity,
        );
    });

    function handleCanvasClick() {
        setIsLarge(!getIsLarge());
    }

    onCleanup(() => {
        chart?.destroy();
    });

    return (
        <div>
        <canvas ref={setCanvas} onClick={handleCanvasClick} />
        <div style={{ display: getIsLarge() ? "block" : "none" }}>
            <CityLargeChart city={props.city} data={props.data} query={props.query} close={() => setIsLarge(false)} />
        </div>
        </div>
    );
}