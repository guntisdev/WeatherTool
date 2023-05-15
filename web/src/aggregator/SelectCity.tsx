import { Accessor, Component, Setter } from "solid-js";
import { cityList } from "../consts";

export const SelectCity: Component<{
    getCities: Accessor<Set<string>>,
    setCities: Setter<Set<string>>,
}> = (props) => {
    function handleSelect(e: MouseEvent) {
        if (!e.target) return;
        const target = e.target as HTMLInputElement;
        const { checked: selected, value: city } = target;
        const cityList = new Set([...props.getCities()]);
        if (selected) cityList.add(city);
        else cityList.delete(city);

        props.setCities(cityList);
    }

    return (
        <ul>{cityList.map(city =>
            <label>
            <li>
                <input
                    type="checkbox"
                    name="city"
                    value={city}
                    onClick={handleSelect}
                />
                {city}
            </li>
            </label>
        )}</ul>
    );
};