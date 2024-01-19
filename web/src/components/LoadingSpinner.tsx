import { Component } from "solid-js";

export const LoadingSpinner: Component<{ text: string; }> = ({ text }) => {
    return (
        <div>
            <span class="spinner"></span>
            <span style={{ "padding-left": "16px" }}>{ text }</span>
        </div>
    );
}