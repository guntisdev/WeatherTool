import { Component, createSignal } from 'solid-js'

import { apiHost } from '../consts'
import styles from './harmonie.module.css'
import { GribFile } from './GribFile'
import { LoadingSpinner } from '../components/LoadingSpinner'

export const Harmonie: Component<{}> = () => {
    const [getFileList, setFileList] = createSignal<string[]>([])
    const [getActiveGrib, setActiveGrib] = createSignal('')
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>()
    const [getIsLoading, setIsLoading] = createSignal(true)

    fetch(`${apiHost}/api/show/grib-list`)
        .then(re => re.json())
        .then(fileList => {
            fileList.sort((a: string, b: string) => a < b ? 1 : -1)
            setFileList(fileList)
        })
        .finally(() => setIsLoading(false))

    return <div class={styles.container}>
        <div class={styles.column}>
            <ul class={styles.fileList}>
                {getFileList().map(fileName =>
                    <GribFile 
                        name={fileName}
                        isActive={() => getActiveGrib() === fileName}
                        getCanvas={getCanvas}
                        setIsLoading={setIsLoading}
                        onClick={() => setActiveGrib(fileName)}
                    />
                )}
            </ul>
        </div>
        <div class={styles.column}>
            { getIsLoading() && <LoadingSpinner text='' />}
            <canvas ref={setCanvas} style={{ display: getIsLoading() ? 'none' : 'block' }} />
        </div>
    </div>
}
