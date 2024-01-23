import { Component, createSignal, Resource, Show } from "solid-js";

import "../../css/Result.css"
import { ResultKeyVal, resultOrder, ResultOrderKeys } from "../../consts";
import { SelectOrder } from "../SelectOrder";
import { CityResult } from "./CityResult";
import { QueryResult } from "../helpers";
import { MapView } from "../map/MapView";

export const Result: Component<{
    result: Resource<QueryResult>
}> = ({ result: resultResource }) => {
    const [getOrderKey, setOrderKey] = createSignal<ResultOrderKeys>("A -> Z");
    type ResultView = "text" | "map_1920x1080" | "map_3840x1440";
    const [getResultView, setResultView] = createSignal<ResultView>("text");

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
                        value="text"
                        onClick={() => setResultView("text")}
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
                        value="map 3840x1440"
                        onClick={() => setResultView("map_3840x1440")}
                    />
                </span>
                <span style={{ visibility: getResultView() === "text" ? "visible" : "hidden" }}>
                    <SelectOrder getter={getOrderKey} setter={setOrderKey} />
                </span>
            </div>
            <div class={getResultView() === "text" ? "result-container" : ""}>
                <Show when={getResultView() === "text"}>
                    {cityData().map(([city, data]) =>
                    <CityResult
                        city={city}
                        query={resultResource()!.query}
                        result={data}
                    />)}
                </Show>
                <Show when={getResultView() === "map_1920x1080"}>
                    <MapView type="map_1920x1080" data={cityData} />
                </Show>
                <Show when={getResultView() === "map_3840x1440"}>
                    <MapView type="map_3840x1440" data={cityData} />
                </Show>
            </div>
        </div>
    );
}