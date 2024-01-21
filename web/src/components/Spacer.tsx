import { Component } from "solid-js";

export const Spacer: Component<{ padding?: string; }> = ({ padding }) => {
    return (
        <div style={{ display: "inline-block", padding: padding ?? "5px" }}></div>
    );
}