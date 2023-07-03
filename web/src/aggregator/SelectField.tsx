import { Accessor, Component, Setter } from "solid-js";

import { weatherField } from "../consts";

export const SelectField: Component<{
    getField: Accessor<string>,
    setField: Setter<string>,
}> = (props) => {
    return (
        <div>
            <h4>Select field</h4>
            <select onChange={(e) => props.setField(e.target.value)}>
                { weatherField.map(field =>
                    <option
                        value={field}
                        selected={field === props.getField() ? true : false}
                    >{field}</option>
                )}
            </select>
        </div>
    );
}