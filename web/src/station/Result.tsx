import { Accessor, Component } from "solid-js";

export const Result: Component<{ getCity: Accessor<string|undefined>}> = ({ getCity }) => {
    return (
        <div>
            { getCity() }
        </div>
    );
}