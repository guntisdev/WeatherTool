import { Component } from "solid-js";

export const Station: Component<{}> = (props) => {
    return (
        <div class="fileManager">
            <h2>Station</h2>
            <div class="container">
                <div class="column">
                    1st
                </div>
                <div class="column">
                    2nd
                </div>
                <div class="column">
                    3rd
                </div>
            </div>
        </div>
    );
}