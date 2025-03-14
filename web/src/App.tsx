import { Component } from 'solid-js'
import { Route, Router } from '@solidjs/router'

import styles from './css/menu.module.css';
import { Country } from './pages/country/Country';
import { Cities } from './pages/cities/Cities';
import { Database } from './pages/database/Database';
import { Station } from './pages/station/Station';
import { apiHost } from './consts';
import { WeatherIconStub } from './components/weatherIcons/WeatherIconStub';
import { Harmonie } from './pages/harmonie/Harmonie'
import { LvgmcForecast } from './pages/lvgmc-forecast/LvgmcForecast'

console.log("env:", import.meta.env.MODE)
console.log("api host:", apiHost)

const App: Component = () => {
    const pages = ['station', 'cities', 'latvia', 'database', 'harmonie', 'lvgmc-forecast']
    return (
        <div>
            <div class="grid-1-1">
                <div><WeatherIconStub /></div>
                <div>
                    <ul class={styles.menu}>
                        { pages.map(page =>
                            <li>
                                <a href={page}>{page}</a>
                            </li>
                        )}
                    </ul>
                </div>
            </div>
            <Router>
                <Route path="/" component={Harmonie} />
                <Route path="/harmonie" component={Harmonie} />
                <Route path="/lvgmc-forecast" component={LvgmcForecast} />
                <Route path="/station" component={Station} />
                <Route path="/cities" component={Cities} />
                <Route path="/latvia" component={Country} />
                <Route path="/database" component={Database} />
            </Router>
        </div>
    );
};

export default App;
