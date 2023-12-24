import moment from 'moment';
import { Accessor, batch, Component, createSignal, Setter } from "solid-js";

import "../css/aggregator.css"

export const SelectTimeRange: Component<{
    getStart: Accessor<Date>,
    setStart: Setter<Date>,
    getEnd: Accessor<Date>,
    setEnd: Setter<Date>,
}> = (props) => {
    // true: dateTime, false: date
    const [getDateTimeMode, setDateTimeMode] = createSignal(true);

    function handleClick() {
        const currentMode = getDateTimeMode();
        if (currentMode) {
            const startDate = moment().subtract(1, "days").startOf("day");
            const endDate = moment().subtract(1, "days").endOf("day");
            batch(() => {
                props.setStart(startDate.toDate());
                props.setEnd(endDate.toDate());
            });
        }
        setDateTimeMode(!currentMode);
    }

    return (
        <div>
            <div class="timeRangeTitle">
                <h4>Time range</h4>
                <div><input
                    type="checkbox"
                    onClick={handleClick}
                    checked={getDateTimeMode()}
                /> date+time</div>
            </div>
            { getDateTimeMode() && <DateTimeRange {...props} /> }
            { !getDateTimeMode() && <DateRange {...props} /> }
        </div>
    );
}

const DateRange: Component<{
    getStart: Accessor<Date>,
    setStart: Setter<Date>,
    getEnd: Accessor<Date>,
    setEnd: Setter<Date>,
}> = (props) => {
    const startStr = () => moment(props.getStart()).format("YYYY-MM-DD");
    const endStr = () => moment(props.getEnd()).format("YYYY-MM-DD");

    return (
        <div>
            <p>
                <input
                    type="date"
                    value={startStr()}
                    onChange={e => props.setStart(
                        moment(new Date(e.target.value)).startOf("day").toDate()
                    )}
                /> start
            </p>
            <p>
                <input
                    type="date"
                    value={endStr()}
                    onChange={e => props.setEnd(
                        moment(new Date(e.target.value)).endOf("day").toDate()
                    )}
                /> end
            </p>
        </div>
    );
}

const DateTimeRange: Component<{
    getStart: Accessor<Date>,
    setStart: Setter<Date>,
    getEnd: Accessor<Date>,
    setEnd: Setter<Date>,
}> = (props) => {
    const startStr = () => moment(props.getStart()).format("YYYY-MM-DDTHH:mm");
    const endStr = () => moment(props.getEnd()).format("YYYY-MM-DDTHH:mm");

    return (
        <div>
            <p>
                <input
                    type="datetime-local"
                    value={startStr()}
                    onChange={e => props.setStart(new Date(e.target.value))}
                /> start
            </p>
            <p>
                <input
                    type="datetime-local"
                    value={endStr()}
                    onChange={e => props.setEnd(new Date(e.target.value))}
                /> end
            </p>
        </div>
    );
}