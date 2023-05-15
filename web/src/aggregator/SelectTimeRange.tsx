import moment from 'moment';
import { Accessor, Component, Setter } from "solid-js";

export const SelectTimeRange: Component<{
    getStart: Accessor<Date>,
    setStart: Setter<Date>,
    getEnd: Accessor<Date>,
    setEnd: Setter<Date>,
}> = (props) => {
    const startStr = () => moment(props.getStart()).format("YYYY-MM-DDTHH:mm");
    const endStr = () => moment(props.getEnd()).format("YYYY-MM-DDTHH:mm");

    return (
        <div>
            <h3>Select time range</h3>
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