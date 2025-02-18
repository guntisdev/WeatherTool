import { Accessor, Component, createEffect, createSignal, Setter } from 'solid-js'

import styles from './harmonie.module.css'
import { apiHost } from '../consts'
import { GribMessage } from './interfaces'
import { fetchBuffer } from '../helpers/fetch'
import { drawGrib } from './draw/drawGrib'
import { fetchWindData, isCalculatedWindDirection } from './draw/windDirection'
import { fetchHourPrecipitationData, isCalculatedHourPrecipitation } from './draw/precipitation'

const CROP_BOUNDS = { x: 1906-1-440, y: 895, width: 440, height: 380 }

export const GribFile: Component<{
    name: string,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    setIsLoading: Setter<boolean>,
    getFileGribList: Accessor<GribMessage[]> // specific reference and forecast time (in one file)
    getAllGribLists: Accessor<GribMessage[]>
    getIsCrop: Accessor<boolean>,
    getIsContour: Accessor<boolean>,
    onClick: (name: string) => void,
}> = ({
    name,
    getCanvas,
    setIsLoading,
    getFileGribList,
    getAllGribLists,
    getIsCrop,
    getIsContour,
    onClick,
}) => {
    let cachedMessages: GribMessage[] = []
    let cachedBuffers: Uint8Array[] = []
    let cachedBitmasks: Uint8Array[] = []

    const [getIsActive, setIsActive] = createSignal(false)

    createEffect(async () => {
        setIsLoading(true)
        const cropBounds = getIsCrop() ? CROP_BOUNDS : undefined
        const contour = getIsContour()
        // hack to show loading spinner
        await new Promise(resolve => setTimeout(resolve, 100))
        if (cachedMessages.length === 0) return;
        drawGrib(getCanvas()!, cachedMessages, cachedBuffers, cachedBitmasks, cropBounds, contour)
        setIsLoading(false)
    })

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
            cachedMessages = messages
            cachedBuffers = binaryBuffers.map(b => new Uint8Array(b))
            cachedBitmasks = bitmasks.map(b => new Uint8Array(b))
            const cropBounds = getIsCrop() ? CROP_BOUNDS : undefined 
            drawGrib(getCanvas()!, cachedMessages, cachedBuffers, cachedBitmasks, cropBounds, getIsContour())
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
