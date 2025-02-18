import { Accessor, batch, Component, createSignal, Setter, Signal } from 'solid-js'

import styles from './harmonie.module.css'
import { apiHost } from '../consts'
import { GribMessage } from './interfaces'
import { fetchBuffer } from '../helpers/fetch'
import { fetchWindData, isCalculatedWindDirection } from './draw/windDirection'
import { fetchHourPrecipitationData, isCalculatedHourPrecipitation } from './draw/precipitation'

export const GribFile: Component<{
    name: string,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
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
        const bitmaskSection = grib.sections.find(section => section.id === 6)
        const binarySection = grib.sections.find(section => section.id === 7)
        if (!bitmaskSection || !binarySection) return;

        const bitmaskOffset = bitmaskSection.offset + 6
        const bitmaskLength = bitmaskSection.size - 6
        const bitmaskPromise = bitmaskSection.size > 6
            ? fetchBuffer(`${apiHost}/api/grib/binary-chunk/${bitmaskOffset}/${bitmaskLength}/${name}`).then(b=>[b])
            : Promise.resolve([])

        const binaryOffset = binarySection.offset + 5
        const binaryLength = binarySection.size - 5
        let fetchPromise: Promise<[GribMessage[], ArrayBuffer[], ArrayBuffer[]]> = Promise.all([
                Promise.resolve([grib]),
                fetchBuffer(`${apiHost}/api/grib/binary-chunk/${binaryOffset}/${binaryLength}/${name}`).then(b=>[b]),
                bitmaskPromise,
            ])

        if(isCalculatedWindDirection(grib)) fetchPromise = fetchWindData(grib, getFileGribList(), name)
        if(isCalculatedHourPrecipitation(grib)) fetchPromise = fetchHourPrecipitationData(grib, getAllGribLists())
        
        fetchPromise.then(([messages, binaryBuffers, bitmasks]) => {
            batch(() => {
                setCachedMessages(messages)
                setCachedBuffers(binaryBuffers.map(b => new Uint8Array(b)))
                setCachedBitmasks(bitmasks.map(b => new Uint8Array(b)))
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
