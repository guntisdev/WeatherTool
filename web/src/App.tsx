import { Component, JSXElement, createSignal } from 'solid-js';

import styles from './css/menu.module.css';
import { Country } from './country/Country';
import { Cities } from './cities/Cities';
import { Database } from './database/Database';
import { Station } from './station/Station';
import { apiHost } from './consts';
import { WeatherIconStub } from './components/weatherIcons/WeatherIconStub';

console.log("env:", import.meta.env.MODE);
console.log("api host:", apiHost);

const App: Component = () => {
    const pages: [string, () => JSXElement][] = [
        ["station", () => <Station />],
        ["cities", () => <Cities />],
        ["latvia", () => <Country />],
        ["database", () => <Database />],
    ];
    const [getPageTitle, setPageTitle] = createSignal("station");

    const getPageContent = () => {
        const page = pages.find(p => p[0] === getPageTitle());
        return page ? page[1]() : <div>Page not found!</div>;
    }

    const getActiveCSS = (pageTitle: string) => {
        return getPageTitle() === pageTitle ? styles.active : "";
    }

    return (
        <div>
            <div class="grid-1-1">
                <div><WeatherIconStub /></div>
                <div>
                    <ul class={styles.menu}>
                        { pages.map(([pageTitle]) =>
                            <li
                                class={getActiveCSS(pageTitle)}
                                onClick={() => setPageTitle(pageTitle)}
                            >
                                {pageTitle}
                            </li>
                        )}
                    </ul>
                </div>
            </div>
            { getPageContent() }
        </div>
    );
};

export default App;
