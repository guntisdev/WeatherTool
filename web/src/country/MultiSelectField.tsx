import { Accessor, Component, Setter } from "solid-js";
import { weatherFieldNumeric } from "../consts";

export const MultiSelectField: Component<{
    getFields: Accessor<string[]>,
    setFields: Setter<string[]>,
}> = ({ getFields, setFields }) => {

    function handleSelect(fieldName: string) {
        const fields = [...getFields()];
        if (fields.includes(fieldName)) {
            const removeIndex = fields.indexOf(fieldName);
            fields.splice(removeIndex, 1);
        }
        else fields.push(fieldName);

        setFields(fields);
    }

    function selectAll(e: MouseEvent) {
        if (!e.target) return;
        const target = e.target as HTMLInputElement;
        const selectedFields = target.checked
            ? [...weatherFieldNumeric]
            : [];

        setFields(selectedFields);
    }

    return (
        <>
        Select all <input
                type="checkbox"
                checked={true}
                onClick={selectAll}
            />
        <ul>
            {weatherFieldNumeric.map(fieldName =>
                <li>
                <label> 
                <input
                    type="checkbox"
                    name={fieldName}
                    checked={getFields().includes(fieldName)}
                    onClick={() => handleSelect(fieldName)}
                />
                {fieldName}
                </label>
                </li>
            )}
        </ul>
        </>
    );
}