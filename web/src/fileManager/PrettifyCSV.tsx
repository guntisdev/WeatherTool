import { Component } from "solid-js";

export const PrettifyCSV: Component<{lines: () => string[];}> = ({ lines }) => {
    const header = () => lines().slice(0, 1);
    const content = () => lines().slice(1) ?? [];
    return (
        <div>
            <div>{header()}</div>
            <table>
                {content().map(line => <PrettifyLine line={line}/>)}
            </table>
        </div>
    )
}

const PrettifyLine: Component<{line: string;}> = ({ line }) => {
    const splitLine = line.split(";");
    const params = splitLine.slice(0, 15);
    const phenomena = splitLine.slice(15);
    return (
        <tr>
            { params.map(param => <td>{param}</td>)}
            <td>{ phenomena.join(",") }</td>
        </tr>
    )
}