import moment from "moment";
import { Accessor, Component, createResource, createSignal } from "solid-js";

import { apiHost } from "../../consts";
import { Result } from "./Result";


export const QueryResult: Component<{
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
    getFields: Accessor<string[]>,
}> = (props) => {
    const queryStart = () => moment(props.getStart()).format("YYYYMMDD_HHmm");
    const queryEnd = () => moment(props.getEnd()).format("YYYYMMDD_HHmm"); 
    const [getTimestamp, setTimestamp] = createSignal<number>(0);
    
    const fetchQuery = async (timestamp: number) => {
        if (props.getFields().length === 0) return new Error("ERROR: Select weather parameters!");
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/query/country/${queryStart()}-${queryEnd()}/${props.getFields().join(",")}`);
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
            { queryResource() &&
                <Result result={queryResource} />
            }
        </div>
    );
}