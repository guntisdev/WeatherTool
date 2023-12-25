import { Component, createSignal } from 'solid-js';

import styles from './css/App.module.css';
import { Country } from './country/Country';
import { Cities } from './cities/Cities';
import { Database } from './database/Database';

console.log("env:", import.meta.env.MODE);
console.log("api host:", import.meta.env.VITE_API_HOST);

type Section = "cities" | "country" | "database";

const App: Component = () => {
    const [getSection, setSection] = createSignal<Section>("cities");

    const section = () => {
        switch(getSection()) {
            case "database": return <Database />;
            case "country": return <Country />;
            case "cities": return <Cities />
            default: 
                return <Cities />;
        }
    }

    return (
        <div class={styles.App}>
            <div class={styles.sections}>
                <span onClick={() => setSection("cities")}>cities</span> | 
                <span onClick={() => setSection("country")}>latvia</span> | 
                <span onClick={() => setSection("database")}>database</span>
            </div>
            { section() }
        </div>
    );
};

export default App;
