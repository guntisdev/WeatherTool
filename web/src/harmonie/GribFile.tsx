import { Accessor, Component, createSignal } from 'solid-js'

import styles from './harmonie.module.css'
import { apiHost } from '../consts'

export const GribFile: Component<{
    name: string,
    isActive: Accessor<boolean>,
    onClick: (name: string) => void,
}> = ({ name, isActive, onClick }) => {
    const [getStructure, setStructure] = createSignal<any[]>([])

    function onFileClick() {
        onClick(name)

        if (getStructure().length === 0) {
            fetch(`${apiHost}/api/show/grib/${name}`)
                .then(re => re.json())
                .then(setStructure)
        }
    }

    return <li
        class={isActive() ? styles.active : ''}
        onClick={onFileClick}
    >
        <div class={styles.name}>{ trimName(name) }</div>
        <ul class={styles.meteoParams}>
            { getStructure().map(grib => 
                <li>{ grib.title }</li>
            )}
        </ul>
    </li>

}

function trimName(title: string): string {
    let result = title
    result = result.replace('harmonie_', '')
    result = result.replace('.grib', '')
    return result
}
