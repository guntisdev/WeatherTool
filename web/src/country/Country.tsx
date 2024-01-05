import moment from "moment";
import { createSignal } from "solid-js";

import { QueryResult } from "./result/QueryResult";
import { SelectTimeRange } from "../components/SelectTimeRange";
import { weatherFieldNumeric } from "../consts";
import { MultiSelectField } from "./MultiSelectField";

const nowRounded = new Date(new Date().setMinutes(30));
const dayAgo = moment(nowRounded).subtract(1, "days").subtract(30, "minutes").toDate();

export function Country() {
    const [getStart, setStart] = createSignal(dayAgo);
    const [getEnd, setEnd] = createSignal(nowRounded);
    const [getFields, setFields] = createSignal(weatherFieldNumeric);

    return (
        <div class="fileManager">
            <div class="container">
                <div class="column">
                    <MultiSelectField
                        getFields={getFields}
                        setFields={setFields}
                    />
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