import { Accessor, Component, Setter } from "solid-js";

import { aggregateKey } from "../consts";

export const SelectKey: Component<{
    getKey: Accessor<string>,
    setKey: Setter<string>,
}> = (props) => {
    return (
        <div>
            <h4>Select key</h4>
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