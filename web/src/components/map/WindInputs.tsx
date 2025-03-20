import { Component, Signal } from "solid-js";

export interface WindSignals {
    direction: Signal<string>;
    speed: Signal<string>;
    gusts: Signal<string>;
    roundValues: Signal<boolean>;
}

export const WindInputs: Component<{signals: WindSignals}> = (
    { signals: {
        direction: [getDirection, setDirection], 
        speed: [getSpeed, setSpeed],
        gusts: [getGusts, setGusts],
        roundValues: [getRoundValues, setRoundValues],
    }}
) => {
    return (
    <>
        <div>
            <input
                type="text"
                onInput={e => setDirection(e.target.value)}
            />
            Wind direction
        </div>
        <div>
            <input
                type="text"
                onInput={e => setSpeed(e.target.value)}
            />
            Wind speed
        </div>
        <div>
            <input
                type="text"
                onInput={e => setGusts(e.target.value)}
            />
            Gusts
        </div>
    </>
    );
}