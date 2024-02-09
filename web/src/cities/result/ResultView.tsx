import { Component, createSignal, JSXElement, Resource, Show } from "solid-js";

import "../../css/Result.css"
import { ResultKeyVal, resultOrder, ResultOrderKeys } from "../../consts";
import { SelectOrder } from "../SelectOrder";
import { QueryResult } from "../helpers";
import { MapView } from "../map/MapView";
import { GridResult } from "./GridResult";

type ResultView = "grid" | "list" | "map_1920x1080" | "map_1920x1080_wind" | "map_3840x1440" | "map_3840x1440_wind";
const resultViews: Array<ResultView> = ["grid", "list", "map_1920x1080", "map_1920x1080_wind", "map_3840x1440", "map_3840x1440_wind"];

export const ResultView: Component<{
    result: Resource<QueryResult>
}> = ({ result: resultResource }) => {
    const [getOrderKey, setOrderKey] = createSignal<ResultOrderKeys>("A -> Z");
    const [getResultView, setResultView] = createSignal<ResultView>("grid");

    const cityData = () =>
        Object.entries(resultResource()!.result)
        .map(keyVal => [...keyVal] as ResultKeyVal)
        .sort((a, b) => resultOrder[getOrderKey()](a, b));

    function showViewButton(viewName: string): boolean {
        if (!resultResource()) return false;
        if (["grid", "list"].includes(viewName) && resultResource()!.query.field === "phenomena") return true;
        if (["grid", "list"].includes(viewName) && resultResource()!.query.key !== "List") return true;
        if (!["grid", "list"].includes(viewName) && resultResource()!.query.key === "List") return false;
        if (resultResource()!.query.field === "phenomena") return false;

        return true;
    }

    return (
        <div>
            <div class="resultTitle">
                <h3>Result:</h3>
                <span>
                    {resultViews.map(viewName =>
                        <>
                        <input
                            type="button"
                            class="secondary"
                            value={viewName}
                            disabled={!showViewButton(viewName)}
                            onClick={() => setResultView(viewName)}
                        />
                        &nbsp; | &nbsp;
                        </>
                    )}
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