import { Component } from "solid-js";
import { CityChart } from "./CityChart";
import { DataQuery } from "../helpers";

export const CityResult: Component<{ city: string, query: DataQuery, result: any }> = ({ city, query, result }) => {
    return (
        <div class="result-item">
            <h4>{ city }</h4>
            {
                isDateNumber(result)
                    ? <CityChart city={city} data={result} query={query}/>
                    : result
            }
        </div>
    );
}

function isDateNumber(value: unknown): value is [string, number | null][] {
    if (!Array.isArray(value)) return false;
    for (let i = 0; i < value.length; i++) {
        let item = value[i];
        if (
            !Array.isArray(item)
            || item.length !== 2
            || typeof item[0] !== "string"
            || (typeof item[1] !== "number" && item[1] !== null)
        ) {
            return false;
        }
    }
    return true;
}