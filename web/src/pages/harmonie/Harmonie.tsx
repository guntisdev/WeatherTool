import { Component, createSignal } from 'solid-js'

import { GribFile } from './GribFile'
import { GribMessage } from './interfaces'
import { DrawView } from './DrawView'
import { ReferenceTimes } from './ReferenceTimes'
import { fetchGribList, fetchGribListStructure } from './fetchGrib'

import styles from './harmonie.module.css'
import { SlideShow } from './SlideShow'
import { fetchJson } from '../../helpers/fetch'
import { apiHost } from '../../consts'

export const Harmonie: Component<{}> = () => {
    const [getFileList, setFileList] = createSignal<string[]>([])
    const [getIsLoading, setIsLoading] = createSignal(true)
    const [getIsCrop, setIsCrop] = createSignal(true)
    const [getIsContour, setIsContour] = createSignal(false)
    const [getIsInterpolated, setIsInterpolated] = createSignal(false)
    const [getGribList, setGribList] = createSignal<GribMessage[]>([])
    const [getCanvas, setCanvas] = createSignal<HTMLCanvasElement>()
    const [getImgList, setImgList] = createSignal<[string, ImageBitmap | undefined][]>([])
    const [getRefDate, setRefDate] = createSignal('')

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

    function deleteOldGrib(): void {
        fetchJson(`${apiHost}/api/grib/delete-old-forecasts`)
            .finally(() => window.location.reload())
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
                setIsLoading={setIsLoading}
                getFileList={getFileList}
                getGribList={getGribList}
                options={{ getIsCrop: getIsCrop, getIsContour: getIsContour, getIsInterpolated: getIsInterpolated }}
                imgListSignal={[getImgList, setImgList]}
                setRefDate={setRefDate}
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
            <div class={styles.deleteContainer}>
                <input
                    type='button'
                    value='Delete old grib files'
                    onClick={deleteOldGrib}
                />
            </div>
        </div>
        <div class={styles.column}>
            <SlideShow
                getIsLoading={getIsLoading}
                getCanvas={getCanvas}
                getImgList={getImgList}
                getRefDate={getRefDate}
            />
            <DrawView
                isLoadingSignal={[getIsLoading, setIsLoading]}
                options={{ getIsCrop: getIsCrop, getIsContour: getIsContour, getIsInterpolated: getIsInterpolated }}
                canvasSignal={[getCanvas, setCanvas]}
                cachedMessagesSignal={cachedMessagesSignal}
                cachedBuffersSignal={cachedBuffersSignal}
                cachedBitmasksSignal={cachedBitmasksSignal}
            />
        </div>
    </div>
}
