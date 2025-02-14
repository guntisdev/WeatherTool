import { Component, createSignal } from 'solid-js'

import { apiHost } from '../consts'
import styles from './harmonie.module.css'
import { GribFile } from './GribFile'
import { LoadingSpinner } from '../components/LoadingSpinner'
import { GribMessage } from './interfaces'
import { fetchJson } from '../helpers/fetch'
import { getFakeWindDirection, isWindSpeed } from './draw/windDirection'

export const Harmonie: Component<{}> = () => {
    const [getFileList, setFileList] = createSignal<string[]>([])
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>()
    const [getIsLoading, setIsLoading] = createSignal(true)
    const [getIsCrop, setIsCrop] = createSignal(true)
    const [getGribList, setGribList] = createSignal<GribMessage[]>([])

    fetchJson(`${apiHost}/api/show/grib-list`)
        .then(fileList => {
            fileList.sort((a: string, b: string) => a < b ? 1 : -1)
            setFileList(fileList)
        })
        .finally(() => setIsLoading(false))

    function onGribMessageClick(fileName: string) {
        if(!getGribList().length) {
            setIsLoading(true)
            fetchJson(`${apiHost}/api/show/grib-all-structure`)
            .then((gribList: GribMessage[]) => {
                setGribList([
                    ...gribList,
                    ...gribList.filter(isWindSpeed).map(getFakeWindDirection),
                ].sort((a, b) => a.title > b.title ? 1 : -1))
            })
            .finally(() => setIsLoading(false))
        }
    }

    function getCurrentGribList(fileName: string): GribMessage[] {
        const [referenceTime, forecastTime] = fileName.replace('harmonie_', '')
            .replace('.grib', '')
            .split('_')
        if (!referenceTime || ! forecastTime) return []

        return getGribList().filter(g =>
            g.time.referenceTime === referenceTime
            && g.time.forecastTime === forecastTime
        )
    }

    return <div class={styles.container}>
        <div class={styles.column}>
            <label>
                Crop Latvia
                <input type='checkbox' checked={getIsCrop()} onChange={()=>setIsCrop(!getIsCrop())} />
            </label>
            <ul class={styles.fileList}>
                {getFileList().map(fileName =>
                    <GribFile 
                        name={fileName}
                        getCanvas={getCanvas}
                        setIsLoading={setIsLoading}
                        getGribList={() => getCurrentGribList(fileName)}
                        getIsCrop={getIsCrop}
                        onClick={() => onGribMessageClick(fileName)}
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
