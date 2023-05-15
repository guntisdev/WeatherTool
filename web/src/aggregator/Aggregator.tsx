import { createSignal } from "solid-js";
import { Result } from "./Result";
import { SelectAggregator } from "./SelectAggregator";
import { SelectCity } from "./SelectCity";
import { SelectTimeRange } from "./SelectTimeRange";

const dayAgoMs = Date.now() - 24 * 60 * 60 * 1000;

export function Aggregator() {
    const [getCities, setCities] = createSignal<Set<string>>(new Set([]));
    const [getStart, setStart] = createSignal(new Date(dayAgoMs));
    const [getEnd, setEnd] = createSignal(new Date());
    const [getField, setField] = createSignal("tempMax");
    const [getKey, setKey] = createSignal("max");

    return (
        <div class="aggregator">
            <h2>Aggregator</h2>
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
                        <SelectAggregator
                            getField={getField}
                            setField={setField}
                            getKey={getKey}
                            setKey={setKey}
                        />
                    </div>
                </div>
                <div class="column">
                    <Result
                        getCities={getCities}
                        getStart={getStart}
                        getEnd={getEnd}
                        getField={getField}
                        getKey={getKey}
                    />
                </div>
            </div>
        </div>
    );
}