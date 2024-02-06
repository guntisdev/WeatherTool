import { Component, createSignal, Resource, Show } from "solid-js";

import "../../css/Result.css"
import { ResultKeyVal, resultOrder, ResultOrderKeys } from "../../consts";
import { SelectOrder } from "../SelectOrder";
import { QueryResult } from "../helpers";
import { MapView } from "../map/MapView";
import { GridResult } from "./GridResult";

export const ResultView: Component<{
    result: Resource<QueryResult>
}> = ({ result: resultResource }) => {
    const [getOrderKey, setOrderKey] = createSignal<ResultOrderKeys>("A -> Z");
    type ResultView = "grid" | "list" | "map_1920x1080" | "map_1920x1080_wind" | "map_3840x1440" | "map_3840x1440_wind";
    const [getResultView, setResultView] = createSignal<ResultView>("grid");

    const cityData = () =>
        Object.entries(resultResource()!.result)
        .map(keyVal => [...keyVal] as ResultKeyVal)
        .sort((a, b) => resultOrder[getOrderKey()](a, b));

    return (
        <div>
            <div class="resultTitle">
                <h3>Result:</h3>
                <span>
                <input
                        type="button"
                        class="secondary"
                        value="grid"
                        onClick={() => setResultView("grid")}
                    /> 
                    &nbsp; | &nbsp;
                    <input
                        type="button"
                        class="secondary"
                        value="list"
                        onClick={() => setResultView("list")}
                    /> 
                    &nbsp; | &nbsp;
                    <input
                        type="button"
                        class="secondary"
                        value="map 1920x1080"
                        onClick={() => setResultView("map_1920x1080")}
                    />
                    &nbsp; | &nbsp;
                    <input
                        type="button"
                        class="secondary"
                        value="map 1920x1080 wind"
                        onClick={() => setResultView("map_1920x1080_wind")}
                    />
                    &nbsp; | &nbsp;
                    <input
                        type="button"
                        class="secondary"
                        value="map 3840x1440"
                        onClick={() => setResultView("map_3840x1440")}
                    />
                    &nbsp; | &nbsp;
                    <input
                        type="button"
                        class="secondary"
                        value="map 3840x1440 wind"
                        onClick={() => setResultView("map_3840x1440_wind")}
                    />
                </span>
                <span style={{ visibility: ["grid", "list"].includes(getResultView()) ? "visible" : "hidden" }}>
                    <SelectOrder getter={getOrderKey} setter={setOrderKey} />
                </span>
            </div>
            <div>
                <Show when={getResultView() === "grid"}>
                    <div class="grid-view">
                        {cityData().map(([city, data]) =>
                        <GridResult
                            city={city}
                            query={resultResource()!.query}
                            result={data}
                        />)}
                    </div>
                </Show>
                <Show when={getResultView() === "list"}>
                    <div class="list-view">
                        {cityData().map(([city, data]) =>
                        <GridResult
                            city={city}
                            query={resultResource()!.query}
                            result={data}
                        />)}
                    </div>
                </Show>
                <Show when={getResultView() === "map_1920x1080"}>
                    <MapView type="map_1920x1080" data={cityData} />
                </Show>
                <Show when={getResultView() === "map_1920x1080_wind"}>
                    <MapView type="map_1920x1080_wind" data={cityData} />
                </Show>
                <Show when={getResultView() === "map_3840x1440"}>
                    <MapView type="map_3840x1440" data={cityData} />
                </Show>
                <Show when={getResultView() === "map_3840x1440_wind"}>
                    <MapView type="map_3840x1440_wind" data={cityData} />
                </Show>
            </div>
        </div>
    );
}