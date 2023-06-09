import { Component, createSignal } from 'solid-js';
import { Aggregator } from './aggregator/Aggregator';

import styles from './css/App.module.css';
import { FileManager } from './fileManager/FileManager';

console.log("env:", import.meta.env.MODE);
console.log("api host:", import.meta.env.VITE_API_HOST);

type Section = "aggregator" | "fileManager";

const App: Component = () => {
    const [getSection, setSection] = createSignal<Section>("aggregator");

    const section = () => getSection() === "aggregator" ? <Aggregator /> : <FileManager />;

    return (
        <div class={styles.App}>
            <div class={styles.sections}>
                <span onClick={() => setSection("aggregator")}>aggregator</span> | 
                <span onClick={() => setSection("fileManager")}>file manager</span>
            </div>
            { section() }
        </div>
    );
};

export default App;
