import moment from "moment";
import { Accessor, Component, createResource, createSignal } from "solid-js";

import "../../css/Result.css"
import { apiHost } from "../../consts";
import { isQueryResult } from "../helpers";
import { Result } from "./Result";

export const QueryResult: Component<{
    getCities: Accessor<Set<string>>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
    getField: Accessor<string>,
    getKey: Accessor<string>,
    getGranularity: Accessor<string>,
}> = (props) => {
    const cities = () => [...props.getCities()].join(",");
    const queryStart = () => moment(props.getStart()).format("YYYYMMDD_HHmm");
    const queryEnd = () => moment(props.getEnd()).format("YYYYMMDD_HHmm"); 
    const [getTimestamp, setTimestamp] = createSignal<number>(0);

    const fetchQuery = async (timestamp: number) => {
        if (cities() === "") return new Error("ERROR: Select cities!");
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/query/city/${cities()}/${queryStart()}-${queryEnd()}/${props.getGranularity()}/${props.getField()}/${props.getKey()}`);
        const json = await response.json();
        return json;
    }

    const [queryResource] = createResource(getTimestamp, fetchQuery);

    return (
        <div>
            <input
                type="button"
                class="primary"
                value="Query Data"
                onClick={() => setTimestamp(Date.now())}
            />
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
            { queryResource() && isQueryResult( queryResource() ) &&
                <Result result={queryResource} />
            }
        </div>
    );
}