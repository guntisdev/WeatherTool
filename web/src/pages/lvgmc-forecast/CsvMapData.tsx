import { Accessor, Component, createEffect, createSignal, onMount } from 'solid-js'
import { apiHost } from '../../consts'
import { fetchText } from '../../helpers/fetch'
import { codeToIcon } from '../../components/weatherIcons/iconConsts'
import { cityCoords } from '../../components/cityCoords'
import { CityData, drawOnMap } from '../../components/map/canvasDraw'
import { citiesToShow, csvCityListInOrder } from './consts'

import arrowUrl from '../../assets/arrow.webp'
import { resolutionProps, windProps, WindProps } from '../../components/map/mapConsts'

export const CsvMapData: Component<{
    getCityLines: Accessor<string[]>,
    getWindLine: Accessor<string>,
    bgImgUrl: string,
}> = ({
    getCityLines,
    getWindLine,
    bgImgUrl,
}) => {
    const [getCityData, setCityData] = createSignal<CityData[]>([])
    const [getWindData, setWindData] = createSignal<[string, string, string]>(['', '', ''])

    createEffect(() => {
        if (getCityLines().length <= 0) return;
        const cityData: CityData[] = csvCityListInOrder
            .map((city, i): CityData => {
                const parts = getCityLines()[i].split(';')
                const temperature = Number(parts[1])
                const iconCode = Number(parts[2])
                const icon = codeToIcon(iconCode)
                const coord = cityCoords[city]
                if (!coord) throw new Error(`No coord found for city: ${city}`)
                return [city, coord, temperature, icon]
            })
            .filter(([city]) => citiesToShow.includes(city))
        
        const windParts = getWindLine().split(';')
        const windSpped = windParts[1]
        const windGusts = windParts[3]
        const windDirection = windParts[5]
        setWindData([windDirection, windSpped, windGusts])
        setCityData(cityData)

        draw()
    })

    const arrowImg = new Image()
    arrowImg.src = arrowUrl

    let canvas: HTMLCanvasElement
    let ctx: CanvasRenderingContext2D
    onMount(() => {
        ctx = canvas!.getContext('2d')!
    })

    function draw(){
        const img = new Image()
        const tmpWindData:  [string, string, string, boolean, WindProps] = [...getWindData(), true, windProps.map_3840x1440_wind]
        img.onload = () => {
            drawOnMap(
                ctx,
                [img, arrowImg],
                getCityData(),
                tmpWindData,
                resolutionProps.map_3840x1440_wind,
            )
        }
        img.src = bgImgUrl
    }

    return <>
        <canvas
            ref={c =>canvas=c}
            width={'3840px'}
            height={'1440px'}
            style={{ 'width': '1000px', 'height': '374px' }}
        />
    </>
}