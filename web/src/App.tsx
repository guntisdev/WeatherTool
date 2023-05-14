import type { Component } from 'solid-js';

import logo from './assets/logo.svg';
import styles from './css/App.module.css';
import { FetchedDates } from './FetchedDates';

console.log("env:", import.meta.env.MODE);
console.log("api host:", import.meta.env.VITE_API_HOST);

const App: Component = () => {
  return (
    <div class={styles.App}>
      <header class={styles.header}>
      <FetchedDates />
        <img src={logo} class={styles.logo} alt="logo" />
        <p>
          Weather tool web
        </p>
      </header>
    </div>
  );
};

export default App;
