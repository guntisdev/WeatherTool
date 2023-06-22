import moment from "moment";
import { Accessor, Component, createResource, createSignal } from "solid-js";

import "../css/Result.css"
import { apiHost, ResultKeyVal, resultOrder, ResultOrderKeys } from "../consts";
import { SelectOrder } from "./SelectOrder";
import { CityResult } from "./CityResult";

export const Result: Component<{
    getCities: Accessor<Set<string>>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
    getField: Accessor<string>,
    getKey: Accessor<string>,
}> = (props) => {
    const cities = () => [...props.getCities()].join(",");
    const queryStart = () => moment(props.getStart()).format("YYYYMMDD_HHmm");
    const queryEnd = () => moment(props.getEnd()).format("YYYYMMDD_HHmm"); 
    const [getTimestamp, setTimestamp] = createSignal<number>(0);

    const fetchQuery = async (timestamp: number) => {
        if (cities() === "") return new Error("ERROR: Select cities!");
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/query/${queryStart()}-${queryEnd()}/${cities()}/${props.getField()}/${props.getKey()}`);
        const json = await response.json();
        return json;
    }

    const [queryResource] = createResource(getTimestamp, fetchQuery);
    const [getOrderKey, setOrderKey] = createSignal<ResultOrderKeys>("A -> Z");

    return (
        <div>
            <input
                type="button"
                class="primary"
                value="Query Data"
                onClick={() => setTimestamp(Date.now())}
            />
            <div class="resultTitle">
                <h3>Result:</h3>
                <SelectOrder getter={getOrderKey} setter={setOrderKey} />
            </div>
            { queryResource.loading && (
                <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>Loading query</span>
                </div>
            )}
            { queryResource.error && (
                <div>Error while querying: ${queryResource.error}</div>
            )}
            { queryResource() && queryResource() instanceof Error &&
                <div>{ queryResource().message }</div>
            }
            { queryResource() && (
                <div class="result-container">
                    { Object.entries(queryResource())
                        .map(keyVal => [...keyVal] as ResultKeyVal)
                        .sort((a, b) => resultOrder[getOrderKey()](a, b))
                        .map(cityData => <CityResult city={cityData[0]} value={cityData[1]} />)
                    }</div>
            )}
        </div>
    );
}