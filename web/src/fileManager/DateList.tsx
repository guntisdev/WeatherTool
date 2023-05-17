import { Accessor, Component, createResource, Setter } from "solid-js";
import { apiHost } from "../consts";

import "../css/spinner.css";
import { Calendar } from "./Calendar";

export const DateList: Component<{getDate: Accessor<Date>, setDate: Setter<Date>;}> = (props) => {
    const fetchDates = async () => {
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/show/all_dates`);
        const json = await response.json();
        return json;
    }

    const [datesResource] = createResource(fetchDates);

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
            { datesResource() && <Calendar
                getSelectedDate={props.getDate}
                setSelectedDate={props.setDate}
                datesWithData={datesResource().map((str: string) => new Date(str))}
            /> }
            
            {/* Backup date view */}
            { datesResource() && (
                <ul>
                    {/* { (datesResource() as any).map((date: any) =>
                        <li onClick={() => clickDate(date)}>{date}</li>
                    )} */}
                </ul>
            )}
        </div>
    );
}
