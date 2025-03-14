import { Component } from 'solid-js'

import styles from './spinner.module.css'

export const LoadingSpinner: Component<{ text: string; }> = ({ text }) => {
    return (
        <div>
            <span class={styles.spinner}></span>
            <span style={{ "padding-left": "16px" }}>{ text }</span>
        </div>
    )
}