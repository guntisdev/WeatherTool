import moment from "moment";
import { Accessor, Component, createResource, createSignal } from "solid-js";

import "../../css/Result.css"
import { FETCH_DELAY_MS, apiHost } from "../../consts";
import { isQueryResult } from "../helpers";
import { LoadingSpinner } from "../../components/LoadingSpinner";
import { ResultView } from "./ResultView";

export const QueryView: Component<{
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
        await new Promise(resolve => setTimeout(resolve, FETCH_DELAY_MS))
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
            { queryResource.loading && <LoadingSpinner text="Loading query" /> }
            { queryResource.error && (
                <div>Error while querying: ${queryResource.error}</div>
            )}
            { queryResource() && queryResource() instanceof Error &&
                <div>{ queryResource().message }</div>
            }
            { queryResource() && isQueryResult( queryResource() ) &&
                <ResultView result={queryResource} />
            }
        </div>
    );
}