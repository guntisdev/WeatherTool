import { Accessor, Component, Setter } from "solid-js";
import { aggregateKey, weatherField } from "../consts";

export const SelectAggregator: Component<{
    getField: Accessor<string>,
    setField: Setter<string>,
    getKey: Accessor<string>,
    setKey: Setter<string>,
}> = (props) => {
    return (
        <div>
            <h3>Select field</h3>
            <select onChange={(e) => props.setField(e.target.value)}>
                { weatherField.map(field =>
                    <option
                        value={field}
                        selected={field === props.getField() ? true : false}
                    >{field}</option>
                )}
            </select>
            <h3>Select aggregator</h3>
            <ul>
                { aggregateKey.map(key => 
                    <label><li><input
                        type="checkbox"
                        name="aggregateKey"
                        value={key}
                        checked={key === props.getKey() ? true : false}
                        onClick={() => props.setKey(key)}
                    />{key}</li></label>
                )}
            </ul>
        </div>
    );
}