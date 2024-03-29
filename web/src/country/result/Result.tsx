import { Component, Resource } from "solid-js";

export interface ResultData {
    [field: string]: [number, number, number, number];
}

export const Result: Component<{
    result: Resource<ResultData>
}> = ({ result: resultResource }) => {
    const countryData = () => Object.entries(resultResource()!)
        .sort((a, b) => a[0] > b[0] ? 1 : -1)
        .map(filterOutSums)
        .map(([field, values]) => <tr>
            <td>{field}</td>
            <td>{values[0]}</td>
            <td>{values[1]}</td>
            <td>{values[2]}</td>
            <td>{values[3]}</td>
            </tr>)

    return (
        <div>
            <table>
                <thead><tr>
                    <td>Param</td>
                    <td>min</td>
                    <td>max</td>
                    <td>avg</td>
                    <td>sum</td>
                </tr></thead>
                { countryData() }
            </table>
        </div>
    );
}

type CountryFieldValues = [number | undefined, number | undefined, number | undefined, number | undefined];

const sumFields = ["precipitation", "snowAvg", "sunDuration"];

function filterOutSums([field, values]: [string, CountryFieldValues]): [string, CountryFieldValues] {
    const modifiedValues: CountryFieldValues = [...values];
    if (!sumFields.includes(field)) modifiedValues[3] = undefined;

    return [field, modifiedValues];
}