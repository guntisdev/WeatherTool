import { Accessor, batch, Component, createSignal, Setter, Signal } from 'solid-js'

import styles from './harmonie.module.css'
import { GribMessage } from './interfaces'
import { fetchGribBinaries } from './fetchGrib'

export const GribFile: Component<{
    name: string,
    setIsLoading: Setter<boolean>,
    getFileGribList: Accessor<GribMessage[]>, // specific reference and forecast time (in one file)
    getAllGribLists: Accessor<GribMessage[]>,
    onClick: (name: string) => void,
    cachedMessagesSignal: Signal<GribMessage[]>,
    cachedBuffersSignal: Signal<Uint8Array[]>,
    cachedBitmasksSignal: Signal<Uint8Array[]>,
}> = ({
    name,
    setIsLoading,
    getFileGribList,
    getAllGribLists,
    onClick,
    cachedMessagesSignal: [, setCachedMessages],
    cachedBuffersSignal: [, setCachedBuffers],
    cachedBitmasksSignal: [, setCachedBitmasks],
}) => {
    const [getIsActive, setIsActive] = createSignal(false)

    function onParamClick(paramId: number) {
        setIsLoading(true);
        const grib = getFileGribList()[paramId]

        fetchGribBinaries(grib, getAllGribLists()).then(([messages, binaryBuffers, bitmasks]) => {
            batch(() => {
                setCachedMessages(messages)
                setCachedBuffers(binaryBuffers)
                setCachedBitmasks(bitmasks)
            })
        })
        .catch(err => console.warn(err.message))
        .finally(() => setIsLoading(false))
    }

    ///// HACK - delete this
    // createEffect(() => {
    //     if (
    //         getFileGribList().length
    //         && name === 'harmonie_2025-02-18T0900Z_2025-02-18T1900Z.grib'
    //     ) {
    //         onParamClick(5)
    //     }
    // })

    return <li
        class={getIsActive() ? styles.active : ''}
        onClick={() => onClick(name)}
    >
        <div class={styles.name} onClick={() => setIsActive(!getIsActive())}>{ trimName(name) }</div>
        <ul class={styles.meteoParams}>
            { getFileGribList()
                .map((grib, i) =>
                <li onClick={() => onParamClick(i)}>{ grib.title.replace('meteorology, ', '') }</li>
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
