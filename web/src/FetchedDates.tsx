import { createResource } from "solid-js";

const apiHost = import.meta.env.VITE_API_HOST;

export function FetchedDates() {
    const fetchDates = async () => {
        const response = await fetch(`${apiHost}/api/show/all_dates`);
        const json = await response.json();
        return json;
    }

    const [datesResource] = createResource(fetchDates);

    return (
        <div>
            { datesResource.loading && (
                <div>Loading dates</div>
            )}
            { datesResource.error && (
                <div>Error while loading dates: ${datesResource.error}</div>
            )}
            { datesResource() && (
                <ul>
                    { (datesResource() as any).map((date: any) => item(date))}
                </ul>
            )}
        </div>
    );
}

function item(date: any) {
    return (
        <li>{date}</li>
    )
}