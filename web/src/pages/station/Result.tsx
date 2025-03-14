import { Accessor, Component, createResource } from "solid-js";
import { FETCH_DELAY_MS, apiHost } from "../../consts";
import moment from "moment";
import { CityChart } from "../../components/chart/CityChart";
import { LoadingSpinner } from '../../components/spinner/LoadingSpinner'

export const Result: Component<{
    getCity: Accessor<string | undefined>,
    getField: Accessor<string>,
    getStart: Accessor<Date>,
    getEnd: Accessor<Date>,
}> = (props) => {
    const queryStart = () => moment(props.getStart()).format("YYYYMMDD_HHmm");
    const queryEnd = () => moment(props.getEnd()).format("YYYYMMDD_HHmm");

    const fetchList = async ([city, field, getStart, getEnd]: [string | undefined, string, Date, Date]) => {
        if (city === undefined) return undefined;
        await new Promise(resolve => setTimeout(resolve, FETCH_DELAY_MS));
        const response = await fetch(`${apiHost}/api/query/city/${city}/${queryStart()}-${queryEnd()}/hour/${field}/list`);
        const json = await response.json();
        return json;
    }

    const fetchMeteo = async ([city, getStart, getEnd]: [string | undefined, Date, Date]) => {
        if (city === undefined) return undefined;
        await new Promise(resolve => setTimeout(resolve, FETCH_DELAY_MS));
        const response = await fetch(`${apiHost}/api/query/city/${city}/${queryStart()}-${queryEnd()}/allFields`);
        const json = await response.json();
        return json;
    }

    const cityFieldDate = (): [string | undefined, string, Date, Date] => [
        props.getCity(),
        props.getField(),
        props.getStart(),
        props.getEnd(),
    ];
    const cityDate = (): [string | undefined, Date, Date] => [
        props.getCity(),
        props.getStart(),
        props.getEnd(),
    ]
    const [listResource] = createResource(cityFieldDate, fetchList);
    const [meteoResource] = createResource(cityDate, fetchMeteo);

    return (
        <div>
            {
                props.getCity() === undefined && <div>Select city!</div>
            }

            { /* City weather field as chart  */}
            { listResource.loading && <LoadingSpinner text="Loading chart" /> }
            { listResource.error && (
                <div>Error while querying: ${listResource.error}</div>
            )}
            { listResource() && listResource() instanceof Error &&
                <div>{ listResource().message }</div>
            }
            { listResource() && props.getCity() && !listResource.loading &&
                <div>
                    <h3>{ props.getCity() }: { props.getField() }</h3>
                    <CityChart
                        city={() => listResource().query.cities[0]}
                        data={() => listResource().result[listResource().query.cities[0]] ?? []}
                        query={() => listResource().query}
                    />
                </div>
            }

            { /* City all weather fields with double values */}
            { meteoResource.loading && <LoadingSpinner text="Loading meteo data" /> }
            { meteoResource.error && (
                <div>Error while querying: ${meteoResource.error}</div>
            )}
            { meteoResource() && meteoResource() instanceof Error &&
                <div>{ meteoResource().message }</div>
            }
            { meteoResource() && props.getCity() && !meteoResource.loading &&
                <ul class="stationResult">
                    { Object.entries(meteoResource())
                    .sort((a, b) => a[0] > b[0] ? 1 : -1)
                    .map(([key, value]: any) =>
                        <li>{key}: {toStringFloat(value)}</li>
                    )}
                </ul>
            }
        </div>
    );
}

function toStringFloat(val: any): string {
    const numVal = parseFloat(val);
    if (isNaN(numVal)) return "";

    return (Math.round(numVal * 10) / 10) + "";
}