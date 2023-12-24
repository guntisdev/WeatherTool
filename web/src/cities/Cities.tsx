import moment from "moment";
import { createSignal } from "solid-js";
import { QueryResult } from "./result/QueryResult";

import { SelectCity } from "./SelectCity";
import { SelectField } from "./SelectField";
import { SelectGranularity } from "./SelectGranularity";
import { SelectKey } from "./SelectKey";
import { SelectTimeRange } from "../components/SelectTimeRange";

const nowRounded = new Date(new Date().setMinutes(30));
const dayAgo = moment(nowRounded).subtract(1, "days").subtract(30, "minutes").toDate();

export function Cities() {
    const [getCities, setCities] = createSignal<Set<string>>(new Set([]));
    const [getStart, setStart] = createSignal(dayAgo);
    const [getEnd, setEnd] = createSignal(nowRounded);
    const [getField, setField] = createSignal("tempMax");
    const [getKey, setKey] = createSignal("max");
    const [getGranularity, setGranularity] = createSignal("hour");

    return (
        <div class="aggregator">
            <h2>Cities</h2>
            <div class="container">
                <div class="column">
                    <SelectCity getCities={getCities} setCities={setCities} />
                </div>
                <div class="column">
                    <div>
                        <SelectTimeRange
                            getStart={getStart}
                            setStart={setStart}
                            getEnd={getEnd}
                            setEnd={setEnd}
                        />
                    </div>
                    <div>
                        <SelectField getField={getField} setField={setField} />
                        <SelectKey getKey={getKey} setKey={setKey} />
                        <SelectGranularity getGranularity={getGranularity} setGranularity={setGranularity} />
                    </div>
                </div>
                <div class="column">
                    <QueryResult
                        getCities={getCities}
                        getStart={getStart}
                        getEnd={getEnd}
                        getField={getField}
                        getKey={getKey}
                        getGranularity={getGranularity}
                    />
                </div>
            </div>
        </div>
    );
}