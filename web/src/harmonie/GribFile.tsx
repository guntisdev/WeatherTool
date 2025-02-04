import { Accessor, Component, createEffect, createMemo, createSignal, Setter } from 'solid-js'

import styles from './harmonie.module.css'
import { apiHost } from '../consts'
import { GribMessage } from './interfaces'
import { fetchBuffer } from '../helpers/fetch'
import { drawGrib } from './draw/drawGrib'
import { fetchWindData, getFakeWindDirection } from './draw/windDirection'

const CROP_BOUNDS = { x: 1906-1-400, y: 950, width: 400, height: 300 }

export const GribFile: Component<{
    name: string,
    isActive: Accessor<boolean>,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    setIsLoading: Setter<boolean>,
    getIsCrop: Accessor<boolean>,
    onClick: (name: string) => void,
}> = ({
    name,
    isActive,
    getCanvas,
    setIsLoading,
    getIsCrop,
    onClick,
}) => {
    const [getGribList, setGribList] = createSignal<GribMessage[]>([])

    function onFileClick() {
        onClick(name)

        if (getGribList().length === 0) {
            fetch(`${apiHost}/api/show/grib/${name}`)
                .then(re => re.json())
                .then((gribList: GribMessage[]) => {
                    const windSpeed = gribList.find(m => m.meteo.discipline===0 && m.meteo.category===2 && m.meteo.product===1)
                    if (windSpeed) gribList.push(getFakeWindDirection(windSpeed))
                    setGribList(gribList)
                })
        }
    }

    let cachedMessages: GribMessage[] = []
    let cachedBuffers: Uint8Array[] = []
    let cachedBitmasks: Uint8Array[] = []

    createEffect(async () => {
        setIsLoading(true)
        const cropBounds = getIsCrop() ? CROP_BOUNDS : undefined
        // hack to show loading spinner
        await new Promise(resolve => setTimeout(resolve, 100))
        if (cachedMessages.length === 0) return;
        drawGrib(getCanvas()!, cachedMessages, cachedBuffers, cachedBitmasks, cropBounds)
        setIsLoading(false)
    })

    function onParamClick(paramId: number) {
        setIsLoading(true);
        const grib = getGribList()[paramId]
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
        const fetchPromise: Promise<[GribMessage[], ArrayBuffer[], ArrayBuffer[]]> = grib.meteo.discipline === 0 && grib.meteo.category === 2 && grib.meteo.product === 192
            ? fetchWindData(grib, getGribList(), name)
            : Promise.all([
                Promise.resolve([grib]),
                fetchBuffer(`${apiHost}/api/grib/binary-chunk/${binaryOffset}/${binaryLength}/${name}`).then(b=>[b]),
                bitmaskPromise,
            ])
        
        fetchPromise.then(([messages, binaryBuffers, bitmasks]) => {
            cachedMessages = messages
            cachedBuffers = binaryBuffers.map(b => new Uint8Array(b))
            cachedBitmasks = bitmasks.map(b => new Uint8Array(b))
            const cropBounds = getIsCrop() ? CROP_BOUNDS : undefined 
            drawGrib(getCanvas()!, cachedMessages, cachedBuffers, cachedBitmasks, cropBounds)
        })
        .catch(err => console.warn(err.message))
        .finally(() => setIsLoading(false))
    }

    return <li
        class={isActive() ? styles.active : ''}
        onClick={onFileClick}
    >
        <div class={styles.name}>{ trimName(name) }</div>
        <ul class={styles.meteoParams}>
            { getGribList()
                .sort((a, b) => a.title > b.title ? 1 : -1)
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
