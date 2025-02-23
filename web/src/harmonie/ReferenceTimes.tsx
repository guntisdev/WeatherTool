import { Accessor, Component, createSignal, resetErrorBoundaries, Setter, Signal } from 'solid-js'

import { DrawOptions, GribMessage } from './interfaces'
import { fetchGribBinaries } from './fetchGrib'
import { drawGrib } from './draw/drawGrib'
import { CROP_BOUNDS } from './DrawView'

import styles from './harmonie.module.css'
import { handleProgressivePromises } from '../helpers/progressivePromises'

export const ReferenceTimes: Component<{
    setIsLoading: Setter<boolean>,
    getFileList: Accessor<string[]>,
    getGribList: Accessor<GribMessage[]>,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    options: DrawOptions,
    imgListSignal: Signal<[string, ImageBitmap | undefined][]>,
    onClick: () => void,
}> = ({
    setIsLoading,
    getFileList,
    getGribList,
    getCanvas,
    options,
    imgListSignal: [getImgList, setImgList],
    onClick,
}) => {
    const [getActiveDate, setActiveDate] = createSignal('')
    const dateList = (): [string, number][] => {
        const datesStr = getFileList().map(f => f.replace('harmonie_', '').split('_')[0])
        const uniqueDates = [...new Set(datesStr)]
        return uniqueDates.map(dateStr => [
            dateStr,
            datesStr.filter(d => d === dateStr).length
        ])
    }

    function onActiveDate(date: string) {
        onClick()
        const newValue = getActiveDate() === date ? '' : date
        setActiveDate(newValue)
    }

    async function fetchDrawImgList(refDateStr: string) {
        setIsLoading(true)
        const canvas = getCanvas()!
        const cropBounds = options.getIsCrop() ? CROP_BOUNDS : undefined
        const contour = options.getIsContour()
        const isInterpolated = options.getIsInterpolated()
        const forecastList = getGribList()
            .filter(g => g.time.referenceTime === refDateStr)
            .filter(g => g.meteo.discipline === 0 && g.meteo.category === 1 && g.meteo.product === 192)
        const emptyImgList: [string, undefined][] = forecastList
            .map((grib): [string, undefined] => [grib.time.forecastTime, undefined])
            .sort((a, b) => a[0] > b[0] ? 1 : -1)
        setImgList(emptyImgList)
        const promiseList = forecastList.map(async (grib): Promise<[string, ImageBitmap]> => {
            const [messages, buffers, bitmasks] = await fetchGribBinaries(grib, getGribList())
            drawGrib(canvas, messages, buffers, bitmasks, cropBounds, contour, isInterpolated)
            // just to be sure that draws
            await new Promise(resolve => setTimeout(resolve))
            const img = await createImageBitmap(canvas)
            return [grib.time.forecastTime, img]
        })

        handleProgressivePromises(
            promiseList,
            ([forecastDate, img]) => {
                const udpdatedImgList = [...getImgList()]
                const idx = udpdatedImgList.findIndex(([d]) => forecastDate === d)
                if (idx >= 0) udpdatedImgList[idx][1] = img
                setImgList(udpdatedImgList)
            },
        ).finally(() => {
            console.log("FINALLLY")
            const ctx = canvas.getContext('2d')!
            ctx.clearRect(0, 0, canvas.width, canvas.height)
            setIsLoading(false)
        })
    }

    return <ul class={styles.dateList}>
        { dateList().map(([dateStr, count]) =>
            <li>
                <div onClick={() => onActiveDate(dateStr)}>
                    { dateStr } ({ count })
                </div>
                <div
                    class={styles.controls}
                    style={{ display: getActiveDate() === dateStr ? 'block' : 'none'}}
                    onClick={() => fetchDrawImgList(dateStr)}
                >
                    {
                        getGribList().some(g => g.time.referenceTime === dateStr)
                        && <>TODO: download all forecasts</>
                    }
                </div>
            </li>)}
    </ul>
}