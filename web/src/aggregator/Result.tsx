import moment from "moment";
import { Accessor, Component, createResource, createSignal } from "solid-js";
import { apiHost } from "../consts";

export const Result: Component<{
    getCities: Accessor<Set<string>>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
    getField: Accessor<string>,
    getKey: Accessor<string>,
}> = (props) => {
    const cities = () => [...props.getCities()].join(",")
    const startStr = () => moment(props.getStart()).format("YYYY/MM/DD, HH:mm");
    const endStr = () => moment(props.getEnd()).format("YYYY/MM/DD, HH:mm");
    const queryStart = () => moment(props.getStart()).format("YYYYMMDD_HHmm");
    const queryEnd = () => moment(props.getEnd()).format("YYYYMMDD_HHmm"); 
    const [getTimestamp, setTimestamp] = createSignal<number>(0);

    const fetchQuery = async (timestamp: number) => {
        if (cities() === "") return "ERROR: Select cities!";
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/query/${queryStart()}-${queryEnd()}/${cities()}/${props.getField()}/${props.getKey()}`);
        const json = await response.json();
        return json;
    }

    const [queryResource] = createResource(getTimestamp, fetchQuery);

    return (
        <div>
            <h3>Aggregate by:</h3>
            <input type="button" value="Query Data" onClick={() => setTimestamp(Date.now())} />
            <p>cities: { cities() }</p>
            <p>time range: { startStr() } - { endStr() }</p>
            <p>weather field: { props.getField() }</p>
            <p>aggregation field: { props.getKey() }</p>
            <h3>Result:</h3>
            <p>
                { queryResource.loading && (
                    <div>
                        <span class="spinner"></span>
                        <span style={{ "padding-left": "16px" }}>Loading query</span>
                    </div>
                )}
                { queryResource.error && (
                    <div>Error while querying: ${queryResource.error}</div>
                )}
                { queryResource() && (
                    <span>{JSON.stringify(queryResource())}</span>
                )}
            </p>
        </div>
    );
}