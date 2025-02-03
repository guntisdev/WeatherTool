import { Accessor, Component, createSignal } from 'solid-js'

import styles from './harmonie.module.css'
import { apiHost } from '../consts'
import { GribMessage } from './interfaces'
import { fetchBuffer } from '../helpers/fetch'
import { drawGrib } from './draw/drawGrib'

const CROP_BOUNDS = { x: 1906-1-400, y: 950, width: 400, height: 300 }

export const GribFile: Component<{
    name: string,
    isActive: Accessor<boolean>,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    onClick: (name: string) => void,
}> = ({ name, isActive, getCanvas, onClick }) => {
    const [getStructure, setStructure] = createSignal<GribMessage[]>([])
    const isCrop = createSignal(false)

    function onFileClick() {
        onClick(name)

        if (getStructure().length === 0) {
            fetch(`${apiHost}/api/show/grib/${name}`)
                .then(re => re.json())
                .then(setStructure)
        }
    }

    let cachedMessages: GribMessage[] = []
    let cachedBuffers: Uint8Array[] = []
    let cachedBitmasks: Uint8Array[] = []

    function onParamClick(paramId: number) {
        const grib = getStructure()[paramId]
        console.log("onParamClick", grib)
        const bitmaskSection = grib.sections.find(section => section.id === 6)
        const binarySection = grib.sections.find(section => section.id === 7)
        if (!bitmaskSection || !binarySection) return;
        console.log("sections 6,7")

        const bitmaskOffset = bitmaskSection.offset + 6
        const bitmaskLength = bitmaskSection.size - 6
        const bitmaskPromise = bitmaskSection.size > 6
            ? fetchBuffer(`${apiHost}/api/grib/binary-chunk/${bitmaskOffset}/${bitmaskLength}/${name}`).then(b=>[b])
            : Promise.resolve([])

        const binaryOffset = binarySection.offset + 5
        const binaryLength = binarySection.size - 5
        const fetchPromise: Promise<[GribMessage[], ArrayBuffer[], ArrayBuffer[]]> = 
            Promise.all([
                [grib],
                fetchBuffer(`${apiHost}/api/grib/binary-chunk/${binaryOffset}/${binaryLength}/${name}`).then(b=>[b]),
                bitmaskPromise,
            ])
        
        fetchPromise.then(([messages, binaryBuffers, bitmasks]) => {
            cachedMessages = messages
            cachedBuffers = binaryBuffers.map(b => new Uint8Array(b))
            cachedBitmasks = bitmasks.map(b => new Uint8Array(b))
            const colors: [string, string] = ['#0000ff', '#ffff00']
            const cropBounds = isCrop[0]() ? CROP_BOUNDS : undefined 
            drawGrib(getCanvas()!, cachedMessages, cachedBuffers, cachedBitmasks, colors, cropBounds)
        })
        .catch(err => console.warn(err.message))
        // .finally(() => setIsLoading(false))
    }

    return <li
        class={isActive() ? styles.active : ''}
        onClick={onFileClick}
    >
        <div class={styles.name}>{ trimName(name) }</div>
        <ul class={styles.meteoParams}>
            { getStructure()
                .sort((a, b) => a.title > b.title ? 1 : -1)
                .map((grib, i) => 
                <li onClick={() => onParamClick(i)}>{ grib.title }</li>
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
