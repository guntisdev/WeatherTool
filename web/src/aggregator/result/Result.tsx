import { Component, createSignal, Resource } from "solid-js";

import "../../css/Result.css"
import { ResultKeyVal, resultOrder, ResultOrderKeys } from "../../consts";
import { SelectOrder } from "../SelectOrder";
import { CityResult } from "../chart/CityResult";
import { QueryResult } from "../helpers";
import { MapView } from "../map/MapView";

export const Result: Component<{
    result: Resource<QueryResult>
}> = ({ result: resultResource }) => {
    const [getOrderKey, setOrderKey] = createSignal<ResultOrderKeys>("A -> Z");
    type ResultView = "text" | "map";
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
                        value="map"
                        onClick={() => setResultView("map")}
                    />
                </span>
                <span style={{ visibility: getResultView() === "text" ? "visible" : "hidden" }}>
                    <SelectOrder getter={getOrderKey} setter={setOrderKey} />
                </span>
            </div>
            <div class={getResultView() === "text" ? "result-container" : ""}>
                { getResultView() === "text"
                    ? cityData().map(([city, data]) =>
                        <CityResult
                            city={city}
                            query={resultResource()!.query}
                            result={data}
                        />)
                    : <MapView data={cityData} />
                }
            </div>
        </div>
    );
}