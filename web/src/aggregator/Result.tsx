import moment from "moment";
import { Accessor, Component, createResource, createSignal } from "solid-js";

import { apiHost, ResultKeyVal, resultOrder, ResultOrderKeys } from "../consts";
import { CityChart } from "./CityChart";

import "../css/Result.css"
import { SelectOrder } from "./SelectOrder";

export const Result: Component<{
    getCities: Accessor<Set<string>>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
    getField: Accessor<string>,
    getKey: Accessor<string>,
}> = (props) => {
    const cities = () => [...props.getCities()].join(",");
    const citiesStr = () => [...props.getCities()].join(", ");
    const startStr = () => moment(props.getStart()).format("YYYY/MM/DD, HH:mm");
    const endStr = () => moment(props.getEnd()).format("YYYY/MM/DD, HH:mm");
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
            <h3>Aggregate by:</h3>
            <input
                type="button"
                class="primary"
                value="Query Data"
                onClick={() => setTimestamp(Date.now())}
            />
            <p>cities: { citiesStr() }</p>
            <p>time range: { startStr() } - { endStr() }</p>
            <p>weather field: { props.getField() }</p>
            <p>aggregation field: { props.getKey() }</p>
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
                        .map(cityData => <ExportCity city={cityData[0]} value={cityData[1]} />)
                    }</div>
            )}
        </div>
    );
}

const ExportCity: Component<{ city: string, value: any }> = ({ city, value }) => {
    return (
        <div class="result-item">
            <h4>{ city }</h4>
            {
                isDateNumber(value)
                ? <CityChart data={value}/>
                : value
            }
        </div>
    );
}

function isDateNumber(value: unknown): value is [string, number | null][] {
    if (!Array.isArray(value)) return false;
    for (let i = 0; i < value.length; i++) {
        let item = value[i];
        if (!Array.isArray(item) || item.length !== 2 || typeof item[0] !== 'string' || (typeof item[1] !== 'number' && item[1] !== null)) {
            return false;
        }
    }
    return true;
}