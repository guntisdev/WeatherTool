import { Accessor, Component, createSignal, resetErrorBoundaries, Setter } from 'solid-js'

import { DrawOptions, GribMessage } from './interfaces'
import { fetchGribBinaries } from './fetchGrib'
import { drawGrib } from './draw/drawGrib'
import { CROP_BOUNDS } from './DrawView'

import styles from './harmonie.module.css'

export const ReferenceTimes: Component<{
    setIsLoading: Setter<boolean>,
    getFileList: Accessor<string[]>,
    getGribList: Accessor<GribMessage[]>,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    options: DrawOptions,
    setImgList: Setter<[string, ImageBitmap][]>,
    onClick: () => void,
}> = ({
    setIsLoading,
    getFileList,
    getGribList,
    getCanvas,
    options,
    setImgList,
    onClick,
}) => {
    const [getActiveDate, setActiveDate] = createSignal('')
    const dateList = () => {
        const datesStr = getFileList().map(f => f.replace('harmonie_', '').split('_')[0])
        return [...new Set(datesStr)]
    }

    function onActiveDate(date: string) {
        onClick()
        const newValue = getActiveDate() === date ? '' : date
        setActiveDate(newValue)
    }

    async function getImgList(dateStr: string) {
        setIsLoading(true)
        const canvas = getCanvas()!
        const cropBounds = options.getIsCrop() ? CROP_BOUNDS : undefined
        const contour = options.getIsContour()
        const isInterpolated = options.getIsInterpolated()
        const forecastList = getGribList()
            .filter(g => g.time.referenceTime === dateStr)
            .filter(g => g.meteo.discipline === 0 && g.meteo.category === 1 && g.meteo.product === 192)
            // .slice(0, 5) // TODO remove this
        const promiseList = forecastList.map(async (grib): Promise<[string, ImageBitmap]> => {
            const [messages, buffers, bitmasks] = await fetchGribBinaries(grib, getGribList())
            drawGrib(canvas, messages, buffers, bitmasks, cropBounds, contour, isInterpolated)
            // just to be sure that draws
            await new Promise(resolve => setTimeout(resolve))
            const img = await createImageBitmap(canvas)
            return [grib.time.forecastTime, img]
        })

        Promise.all(promiseList)
            .then(imgList => {
                imgList.sort((a, b) => a[0] > b[0] ? 1 : -1)
                console.log(imgList)
                setImgList(imgList)
            })
            .finally(() => {
                const ctx = canvas.getContext('2d')!
                ctx.clearRect(0, 0, canvas.width, canvas.height)
                setIsLoading(false)
            })

    }

    return <ul class={styles.dateList}>
        { dateList().map(dateStr =>
            <li>
                <div onClick={() => onActiveDate(dateStr)}>
                    { dateStr }
                </div>
                <div
                    class={styles.controls}
                    style={{ display: getActiveDate() === dateStr ? 'block' : 'none'}}
                    onClick={() => getImgList(dateStr)}
                >
                    {
                        getGribList().some(g => g.time.referenceTime === dateStr)
                        && <>TODO: download all forecasts</>
                    }
                </div>
            </li>)}
    </ul>
}