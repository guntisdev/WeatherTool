import { Accessor, Component, Setter } from "solid-js";

import { aggregateGranularity } from "../consts";

export const SelectGranularity: Component<{
    getGranularity: Accessor<string>,
    setGranularity: Setter<string>,
}> = ({ getGranularity, setGranularity }) => {
    return (
        <div>
            <h4>Select granularity</h4>
            <select onChange={(e) => setGranularity(e.target.value)}>
                { aggregateGranularity.map(granularity =>
                    <option
                        value={granularity}
                        selected={granularity === getGranularity() ? true : false}
                    >{granularity}</option>
                )}
            </select>
        </div>
    );
}