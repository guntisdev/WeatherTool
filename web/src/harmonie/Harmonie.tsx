import { Component, createSignal } from 'solid-js'

import { GribFile } from './GribFile'
import { GribMessage } from './interfaces'
import { DrawView } from './DrawView'
import { ReferenceTimes } from './ReferenceTimes'
import { fetchGribList, fetchGribListStructure } from './fetchGrib'

import styles from './harmonie.module.css'

export const Harmonie: Component<{}> = () => {
    const [getFileList, setFileList] = createSignal<string[]>([])
    const [getIsLoading, setIsLoading] = createSignal(true)
    const [getIsCrop, setIsCrop] = createSignal(true)
    const [getIsContour, setIsContour] = createSignal(true)
    const [getIsInterpolated, setIsInterpolated] = createSignal(true)
    const [getGribList, setGribList] = createSignal<GribMessage[]>([])

    const cachedMessagesSignal = createSignal<GribMessage[]>([])
    const cachedBuffersSignal = createSignal<Uint8Array[]>([])
    const cachedBitmasksSignal = createSignal<Uint8Array[]>([])

    fetchGribList()
        .then(setFileList)
        .finally(() => setIsLoading(false))

    function getAllGribStructure() {
        if(!getGribList().length) {
            setIsLoading(true)
            fetchGribListStructure()
                .then(setGribList)
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
            &nbsp;
            <label>
                Contour
                <input type='checkbox' checked={getIsContour()} onChange={()=>setIsContour(!getIsContour())} />
            </label>
            &nbsp;
            <label>
                Interpolate
                <input type='checkbox' checked={getIsInterpolated()} onChange={()=>setIsInterpolated(!getIsInterpolated())} />
            </label>
            <ReferenceTimes
                getFileList={getFileList}
                getGribList={getGribList}
                onClick={getAllGribStructure}
            />
            <ul class={styles.fileList}>
                {getFileList().map(fileName =>
                    <GribFile 
                        name={fileName}
                        setIsLoading={setIsLoading}
                        getFileGribList={() => getCurrentGribList(fileName)}
                        getAllGribLists={getGribList}
                        onClick={getAllGribStructure}
                        cachedMessagesSignal={cachedMessagesSignal}
                        cachedBuffersSignal={cachedBuffersSignal}
                        cachedBitmasksSignal={cachedBitmasksSignal}
                    />
                )}
            </ul>
        </div>
        <div class={styles.column}>
            <DrawView
                isLoadingSignal={[getIsLoading, setIsLoading]}
                options={{ getIsCrop: getIsCrop, getIsContour: getIsContour, getIsInterpolated: getIsInterpolated }}
                cachedMessagesSignal={cachedMessagesSignal}
                cachedBuffersSignal={cachedBuffersSignal}
                cachedBitmasksSignal={cachedBitmasksSignal}
            />
        </div>
    </div>
}
