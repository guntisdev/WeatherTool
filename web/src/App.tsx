import type { Component } from 'solid-js';

import logo from './assets/logo.svg';
import styles from './css/App.module.css';

const App: Component = () => {
  return (
    <div class={styles.App}>
      <header class={styles.header}>
        <img src={logo} class={styles.logo} alt="logo" />
        <p>
          Weather tool web
        </p>
      </header>
    </div>
  );
};

export default App;
