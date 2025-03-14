import { Component, batch, createSignal } from "solid-js";
import moment from "moment";

import { SelectCity } from "../../components/SelectCity";
import { SelectField } from "../../components/SelectField";
import { Result } from "./Result";
import { MapResult } from "./MapResult";
import "../../css/station.css"
import { Spacer } from "../../components/Spacer";

export const Station: Component<{}> = () => {
    const [getShowCities, setShowCities] = createSignal(false);
    const [getCities, setCities] = createSignal<Set<string>>(new Set(["Ainaži", "Rīga", "Rēzekne", "Liepāja", "Daugavpils", "Ventspils", "Madona"]));
    const [getField, setField] = createSignal("tempMax");
    const [getCity, setCity] = createSignal<string | undefined>("Rīga");
    const [getStart, setStart] = createSignal(moment().subtract(1, "days").toDate());
    const [getEnd, setEnd] = createSignal(moment().toDate());

    function handleDateChange(value: string) {
        const today = moment();
        const inputDate = moment(value, 'YYYY-MM-DD');
        if (today.isSame(inputDate, "date")) {
            batch(() => {
                setStart(today.clone().subtract(1, "days").toDate());
                setEnd(today.toDate());
            });
        } else {
            batch(() => {
                setStart(inputDate.set({ hour: 0, minute: 0, second: 0 }).toDate());
                setEnd(inputDate.set({ hour: 23, minute: 59, second: 59 }).toDate());
            });
        }
    }

    return (
        <div class="stationWrapper">
            <div class="container">
                <div class="column">
                    <div class="submenu">
                        <input
                            type="button"
                            value="Select stations"
                            onClick={() => setShowCities(!getShowCities())}
                        />
                        <Spacer />
                        <input
                            type="date"
                            value={moment(getEnd()).format("YYYY-MM-DD")}
                            onChange={e => handleDateChange(e.target.value)}
                        />
                        <Spacer />
                        <SelectField getField={getField} setField={setField} />
                    </div>
                    <div class={"cities " + (getShowCities() ? "visible" : "hidden")}>
                        <SelectCity getCities={getCities} setCities={setCities} />
                    </div>
                    <MapResult
                        setCity={setCity}
                        getCities={getCities}
                        getField={getField}
                        getStart={getStart}
                        getEnd={getEnd}
                    />
                </div>
                <div class="column">
                    <Result
                        getCity={getCity}
                        getField={getField}
                        getStart={getStart}
                        getEnd={getEnd}
                    />
                </div>
            </div>
        </div>
    );
}