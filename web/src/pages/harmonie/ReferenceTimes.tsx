import { Accessor, Component, createSignal, Setter, Signal } from 'solid-js'

import { CROP_BOUNDS, DrawOptions, GribMessage, MeteoParam } from './interfaces'
import { fetchGribBinaries } from './fetchGrib'
import { drawGrib } from './draw/drawGrib'

import styles from './harmonie.module.css'
import { processPromisesInBatches } from '../../helpers/progressivePromises'

const METEO_PARAMS: [string, MeteoParam][] = [
    ['temperature', { discipline: 0, category: 0, product: 0, levelType: -1, levelValue: -1, subType: 'now' }],
    ['precipitation', { discipline: 0, category: 1, product: 236, levelType: -1, levelValue: -1, subType: 'now' }],
    ['snow depth', { discipline: 0, category: 1, product: 60, levelType: -1, levelValue: -1, subType: 'now' }],
    ['categorical precipitation', { discipline: 0, category: 1, product: 192, levelType: -1, levelValue: -1, subType: 'now' }],
    ['wind speed', { discipline: 0, category: 2, product: 1, levelType: -1, levelValue: -1, subType: 'now' }],
    ['wind speed gust', { discipline: 0, category: 2, product: 22, levelType: -1, levelValue: -1, subType: 'now' }],
    ['wind direction', { discipline: 0, category: 2, product: 192, levelType: -1, levelValue: -1, subType: 'now' }],
]

export const ReferenceTimes: Component<{
    setIsLoading: Setter<boolean>,
    getFileList: Accessor<string[]>,
    getGribList: Accessor<GribMessage[]>,
    options: DrawOptions,
    imgListSignal: Signal<[string, ImageBitmap | undefined][]>,
    setRefDate: Setter<string>,
    onClick: () => void,
}> = ({
    setIsLoading,
    getFileList,
    getGribList,
    options,
    imgListSignal: [getImgList, setImgList],
    setRefDate,
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

    async function fetchDrawImgList(refDateStr: string, param: MeteoParam) {
        setIsLoading(true)
        const cropBounds = options.getIsCrop() ? CROP_BOUNDS : undefined
        const contour = options.getIsContour()
        const isInterpolated = options.getIsInterpolated()
        const forecastList = getGribList()
            .filter(g => g.time.referenceTime === refDateStr)
            .filter(g => g.meteo.discipline === param.discipline && g.meteo.category === param.category && g.meteo.product === param.product)
        const emptyImgList: [string, undefined][] = forecastList
            .map((grib): [string, undefined] => [grib.time.forecastTime, undefined])
            .sort((a, b) => a[0] > b[0] ? 1 : -1)
        setImgList(emptyImgList)
        const promiseFnsList = forecastList.map((grib): () => Promise<[string, ImageBitmap]> => async () => {
            const canvas = document.createElement('canvas')
            const [messages, buffers, bitmasks] = await fetchGribBinaries(grib, getGribList())
            drawGrib(canvas, messages, buffers, bitmasks, cropBounds, contour, isInterpolated)
            // just to be sure that draws
            await new Promise(resolve => setTimeout(resolve))
            const img = await createImageBitmap(canvas)
            return [grib.time.forecastTime, img]
        })

        processPromisesInBatches(
            promiseFnsList,
            ([forecastDate, img]) => {
                const udpdatedImgList = [...getImgList()]
                const idx = udpdatedImgList.findIndex(([d]) => forecastDate === d)
                if (idx >= 0) udpdatedImgList[idx][1] = img
                setImgList(udpdatedImgList)
            },
        ).finally(() => {
            setRefDate(refDateStr)
            setIsLoading(false)
        })
    }

    return <ul class={styles.dateList}>
        { dateList().map(([dateStr, count]) =>
            <li>
                <div onClick={() => onActiveDate(dateStr)}>
                    <b>{ dateStr }</b> ({ count })
                </div>
                <ul
                    class={styles.controls}
                    style={{ display: getActiveDate() === dateStr ? 'block' : 'none'}}
                >
                    { METEO_PARAMS.map(([paramName, param]) =>
                        <li onClick={() => fetchDrawImgList(dateStr, param)}>
                            { paramName }
                        </li>
                    )}
                </ul>
            </li>)}
    </ul>
}