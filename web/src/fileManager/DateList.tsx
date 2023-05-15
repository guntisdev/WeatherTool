import { Component, createResource, Setter } from "solid-js";
import { apiHost } from "../consts";

import "../css/spinner.css";

export const DateList: Component<{setDate: Setter<string>;}> = (props) => {
    const fetchDates = async () => {
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/show/all_dates`);
        const json = await response.json();
        return json;
    }

    const [datesResource] = createResource(fetchDates);

    const clickDate = (date: string) => {
        console.log("setDate", date);
        props.setDate(date);
    }

    return (
        <div>
            { datesResource.loading && (
                <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>Loading dates</span>
                </div>
            )}
            { datesResource.error && (
                <div>Error while loading dates: ${datesResource.error}</div>
            )}
            { datesResource() && (
                <ul>
                    { (datesResource() as any).map((date: any) =>
                        <li onClick={() => clickDate(date)}>{date}</li>
                    )}
                </ul>
            )}
        </div>
    );
}
