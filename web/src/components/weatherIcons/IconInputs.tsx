import { Component, Signal } from "solid-js";
import { weatherIconCities, weatherIcons } from "./iconConsts";

import "../../css/weatherIcons.css";

export const IconInputs: Component<{ weatherIconsSingal: Signal<{[key: string]: string;}>; }> = ({
    weatherIconsSingal: [getWeatherIcons, setWeatherIcons]
}) => {

    function onIconChange(city: string, icon: string) {
        const weatherIcons = { ...getWeatherIcons() };
        weatherIcons[city] = icon;
        if (icon === "") delete weatherIcons[city];

        setWeatherIcons(weatherIcons);
    }

    return (
        <>
            <div>
                { weatherIcons.map(icon =>
                    <span class="weatherIconButton" onClick={() => copyToClipboard(icon)}>{ icon }</span>)
                }
            </div>
            <div>
                { weatherIconCities.map(city =>
                    <div style="float: left;">
                        {city}:
                        &nbsp;
                        <input
                            type="text"
                            class="weatherIconInput"
                            size="1"
                            maxlength="1"
                            onInput={e => onIconChange(city, e.target.value)}
                        />
                        &nbsp;
                    </div>)
                }
            </div>
        </>
    )
}

function copyToClipboard(icon: string) {
    navigator.clipboard.writeText(icon);
}
