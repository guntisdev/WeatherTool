import moment from "moment";
import { Accessor, Component, Setter } from "solid-js";

import "../../css/calendar.css";

export const Calendar: Component<{
    getSelectedDate: Accessor<Date>,
    setSelectedDate: Setter<Date>,
    setCurrentMonth: Setter<Date>,
    datesWithData: () => Date[],
}> = ({getSelectedDate, setSelectedDate, setCurrentMonth, datesWithData}) => {
    const dateStrArr = () => datesWithData().map(d => d.toDateString());

    function changeMonths(delta: number): void {
        const newDate = moment(getSelectedDate()).add(delta, "months").toDate();
        setCurrentMonth(newDate);
        setSelectedDate(newDate);
    }

    return (
        <div>
            <h3>Calendar</h3>
            <div class="calendar-top">
                <button onClick={() => changeMonths(-1) }>&lt;&lt;</button>
                <div class="calendar-year-month">
                    {moment(getSelectedDate()).format("YYYY, MMM")}
                </div>
                <button onclick={() => changeMonths(1)}>&gt;&gt;</button>
            </div>
            <div class="calendar">
                { getPaddedMonth(getSelectedDate()).map(d => {
                    const classArr = ["calendar-cell"];
                    if (d.getMonth() !== getSelectedDate().getMonth()) classArr.push("prev-month");
                    if (dateStrArr().includes(d.toDateString())) classArr.push("data-date");
                    if (d.toDateString() === getSelectedDate().toDateString()) classArr.push("current-date");
                    return (
                        <div
                            class={classArr.join(" ")}
                            onClick={() => setSelectedDate(d)}
                        >
                                {d.getDate()}
                        </div>
                )})}
            </div>
        </div>
    )
}

function getPaddedMonth(date: Date): Date[] {
    const firstDayOfMonth = new Date(new Date(date).setDate(1));
    const datesArr = getMonthDates(firstDayOfMonth);

    // padd start of the month
    const dateIterator = new Date(firstDayOfMonth);
    while (toEuropeanDay(dateIterator.getDay()) > 0) {
        dateIterator.setDate(dateIterator.getDate() - 1);
        datesArr.unshift(new Date(dateIterator));
    }

    // padd end of the month
    dateIterator.setMonth(firstDayOfMonth.getMonth())
    dateIterator.setDate(datesArr[datesArr.length - 1].getDate())
    while (toEuropeanDay(dateIterator.getDay()) < 6) {
        dateIterator.setDate(dateIterator.getDate() + 1);
        datesArr.push(new Date(dateIterator));
    }

    return datesArr;
}

function getMonthDates(firstDayOfMonth: Date): Date[] {
    const date = new Date(firstDayOfMonth);
    const month = date.getMonth();
    const arr: Date[] = [];
    while (date.getMonth() === month) {
        arr.push(new Date(date));
        date.setDate(date.getDate() + 1);
    }
    return arr;
}

function toEuropeanDay(usaDay: number): number {
    return --usaDay === -1 ? 6 : usaDay;
}
