import { Accessor, Component, createResource } from "solid-js";
import { apiHost } from "../consts";
import moment from "moment";
import { CityChart } from "../cities/chart/CityChart";

export const Result: Component<{
    getCity: Accessor<string|undefined>,
    getField: Accessor<string>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
}> = (props) => {
    const queryStart = () => moment(props.getStart()).format("YYYYMMDD_HHmm");
    const queryEnd = () => moment(props.getEnd()).format("YYYYMMDD_HHmm");

    const fetchList = async (city: string | undefined) => {
        if (city === undefined) return undefined;
        await new Promise(resolve => setTimeout(resolve, 500));
        const response = await fetch(`${apiHost}/api/query/city/${props.getCity()}/${queryStart()}-${queryEnd()}/hour/${props.getField()}/list`);
        const json = await response.json();
        return json;
    }

    const fetchMeteo = async (city: string | undefined) => {
        if (city === undefined) return undefined;
        await new Promise(resolve => setTimeout(resolve, 500));
        const response = await fetch(`${apiHost}/api/query/city/${props.getCity()}/${queryStart()}-${queryEnd()}/allFields`);
        const json = await response.json();
        return json;
    }

    const [listResource] = createResource(props.getCity, fetchList);
    const [meteoResource] = createResource(props.getCity, fetchMeteo);

    return (
        <div>
            {
                props.getCity() === undefined && <div>Select city!</div>
            }

            { /* City weather field as chart  */}
            { listResource.loading && (
                <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>Loading chart</span>
                </div>
            )}
            { listResource.error && (
                <div>Error while querying: ${listResource.error}</div>
            )}
            { listResource() && listResource() instanceof Error &&
                <div>{ listResource().message }</div>
            }
            { listResource() && props.getCity() &&
                <div>
                    <h3>{ props.getCity() }</h3>
                    <CityChart
                        city={() => listResource().query.cities[0]}
                        data={() => listResource().result[listResource().query.cities[0]]}
                        query={() => listResource().query}
                    />
                </div>
            }

            { /* City all weather fields with double values */}
            { meteoResource.loading && (
                <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>Loading meteo data</span>
                </div>
            )}
            { meteoResource.error && (
                <div>Error while querying: ${meteoResource.error}</div>
            )}
            { meteoResource() && meteoResource() instanceof Error &&
                <div>{ meteoResource().message }</div>
            }
            { meteoResource() && props.getCity() &&
                <ul>
                    { Object.entries(meteoResource())
                    .sort((a, b) => a[0] > b[0] ? 1 : -1)
                    .map(([key, value]: any) =>
                        <li>{key}: {value}</li>
                    )}
                </ul>
            }
        </div>
    );
}