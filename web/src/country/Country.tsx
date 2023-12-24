import { createSignal } from "solid-js";

import { QueryResult } from "./result/QueryResult";
import { SelectTimeRange } from "../components/SelectTimeRange";
import moment from "moment";
import { weatherField } from "../consts";

const nowRounded = new Date(new Date().setMinutes(30));
const dayAgo = moment(nowRounded).subtract(1, "days").subtract(30, "minutes").toDate();

export function Country() {
    const weatherFieldNumeric = weatherField.filter(f => f !== "phenomena");
    const [getStart, setStart] = createSignal(dayAgo);
    const [getEnd, setEnd] = createSignal(nowRounded);
    const [getFields, setFields] = createSignal(weatherFieldNumeric);

    return (
        <div class="fileManager">
            <h2>Latvia</h2>
            <div class="container">
                <div class="column">
                    column1
                </div>
                <div class="column">
                    <SelectTimeRange
                        getStart={getStart}
                        setStart={setStart}
                        getEnd={getEnd}
                        setEnd={setEnd}
                    />
                </div>
                <div class="column">
                    <QueryResult
                        getFields={getFields}
                        getStart={getStart}
                        getEnd={getEnd}
                    />
                </div>
            </div>
        </div>
    );
}