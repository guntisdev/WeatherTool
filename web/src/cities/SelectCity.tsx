import { Accessor, Component, createEffect, Setter } from "solid-js";
import { cityList } from "../consts";

export const SelectCity: Component<{
    getCities: Accessor<Set<string>>,
    setCities: Setter<Set<string>>,
}> = ({ getCities, setCities }) => {
    function handleSelect(e: MouseEvent) {
        if (!e.target) return;
        const target = e.target as HTMLInputElement;
        const { checked: selected, value: city } = target;
        const selectedCities = new Set([...getCities()]);
        if (selected) selectedCities.add(city);
        else selectedCities.delete(city);

        setCities(selectedCities);
    }

    function selectAll(e: MouseEvent) {
        if (!e.target) return;
        const target = e.target as HTMLInputElement;
        const selectedCities = target.checked
            ? new Set([...cityList])
            : new Set([]);

        setCities(selectedCities);
    }

    return (
        <div>
            Select all <input
                type="checkbox"
                onClick={selectAll}
            />
            <ul>{cityList.map(city =>
                <li>
                    <label>
                    <input
                        type="checkbox"
                        name="city"
                        value={city}
                        checked={getCities().has(city)}
                        onClick={handleSelect}
                    />
                    {city}
                    </label>
                </li>
                )}
            </ul>
        </div>
    );
};