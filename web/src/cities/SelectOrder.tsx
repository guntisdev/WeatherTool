import { Accessor, Component, Setter } from "solid-js";
import { resultOrder, ResultOrderKeys } from "../consts";

export const SelectOrder: Component<{
    getter: Accessor<ResultOrderKeys>,
    setter: Setter<ResultOrderKeys>,
}> = ({ getter, setter }) => {
    return (
        <div>
            Order by:
            <select onChange={e => setter(e.target.value as ResultOrderKeys)}>
                { Object.keys(resultOrder).map(orderKey => 
                    <option
                        value={orderKey}
                        selected={orderKey === getter()}
                    >
                        {orderKey}
                    </option>
                )}
            </select>
        </div>
    );
};