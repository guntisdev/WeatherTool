import { Component, Signal } from "solid-js";

export interface WindSignals {
    direction: Signal<string>;
    speed: Signal<string>;
    gusts: Signal<string>;
}

export const WindInputs: Component<{signals: WindSignals}> = (
    { signals: {
        direction: [getDirection, setDirection], 
        speed: [getSpeed, setSpeed],
        gusts: [getGusts, setGusts],
    }}
) => {
    return (
    <>
        <div>
            <input type="text" onInput={e => setDirection(e.target.value)} />
            Vēja virziens
        </div>
        <div>
            <input type="text" onInput={e => setSpeed(e.target.value)} />
            Vēja ātrums
        </div>
        <div>
            <input type="text" onInput={e => setGusts(e.target.value)} />
            Brāzmas
        </div>
    </>
    );
}