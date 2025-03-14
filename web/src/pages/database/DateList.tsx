import moment from "moment"
import { Accessor, Component, createResource, createSignal, Setter } from "solid-js"

import { apiHost, FETCH_DELAY_MS } from "../../consts"
import { Calendar } from "./Calendar"
import { LoadingSpinner } from "../../components/spinner/LoadingSpinner"

export const DateList: Component<{getDate: Accessor<Date>, setDate: Setter<Date>;}> = (props) => {
    const [getCurrentMonth, setCurrentMonth] = createSignal(new Date());
    const fetchDates = async () => {
        const months = [
            moment(getCurrentMonth()).format("yyyyMM"),
            moment(getCurrentMonth()).subtract(1, "months").format("yyyyMM"),
            moment(getCurrentMonth()).add(1, "months").format("yyyyMM"),
        ];
        await new Promise(resolve => setTimeout(resolve, FETCH_DELAY_MS))
        const response = await fetch(`${apiHost}/api/show/months/${months.join(",")}`);
        const json = await response.json();
        return json;
    }

    const [datesResource] = createResource(getCurrentMonth, fetchDates);

    return (
        <div>
            { datesResource.loading && <LoadingSpinner text="Loading calendar..." /> }
            { datesResource.error && (
                <div>Error while loading dates: ${datesResource.error}</div>
            )}
            { !datesResource.loading && datesResource() && <Calendar
                getSelectedDate={props.getDate}
                setSelectedDate={props.setDate}
                setCurrentMonth={setCurrentMonth}
                datesWithData={() => datesResource().map((str: string) => new Date(str))}
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
