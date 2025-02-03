import { Component, createSignal } from 'solid-js'
import { apiHost } from '../consts'

import styles from './harmonie.module.css'
import { GribFile } from './GribFile'

export const Harmonie: Component<{}> = () => {
    const [getFileList, setFileList] = createSignal<string[]>([])
    const [getActiveGrib, setActiveGrib] = createSignal('')
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>()

    fetch(`${apiHost}/api/show/grib-list`)
        .then(re => re.json())
        .then(fileList => {
            fileList.sort((a: string, b: string) => a < b ? 1 : -1)
            setFileList(fileList)
        })

    return <div class={styles.container}>
        <div class={styles.column}>
            <ul class={styles.fileList}>
                {getFileList().map(fileName =>
                    <GribFile 
                        name={fileName}
                        isActive={() => getActiveGrib() === fileName}
                        getCanvas={getCanvas}
                        onClick={() => setActiveGrib(fileName)}
                    />
                )}
            </ul>
        </div>
        <div class={styles.column}>
            <canvas ref={setCanvas} />
        </div>
    </div>
}
