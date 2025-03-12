import { Component, createEffect, createSignal, onMount } from 'solid-js'
import { apiHost } from '../consts'
import { fetchText } from '../helpers/fetch'
import { codeToIcon } from '../components/weatherIcons/iconConsts'
import { cityCoords } from '../components/cityCoords'
import { CityData, drawOnMap } from '../cities/map/canvasDraw'

import arrowUrl from '../assets/arrow.webp'
import bgMap from '../assets/map_3840x1440_wind.webp'
import { resolutionProps, WindProps, windProps } from '../cities/map/mapConsts'

//  Eiropa_LTV_pilsetas_nakama_dn.csv
//  Eiropa_LTV_pilsetas_tekosa_dn.csv
//  Latvija_LTV_pilsetas_nakama_dnn.csv
//  Latvija_LTV_pilsetas_tekosa_dn.csv
//  Latvija_faktiskais_laiks.csv

export const csvCityListInOrder = ['Alūksne', 'Bauska', 'Cēsis', 'Daugavpils', 'Jelgava', 'Rīga', 'Liepāja', 'Madona', 'Rēzekne', 'Saldus', 'Valmiera', 'Ventspils', 'Ainaži', 'Dobele', 'Gulbene', 'Sigulda', 'Talsi', 'Jēkabpils']
export const citiesToShow = ['Liepāja', 'Ventspils', 'Saldus', 'Talsi', 'Bauska', 'Rīga', 'Ainaži', 'Valmiera', 'Alūksne', 'Madona', 'Jēkabpils', 'Daugavpils']


export const LvgmcForecast: Component<{}> = () => {
    const [getCityData, setCityData] = createSignal<CityData[]>([])
    const [getWindData, setWindData] = createSignal<[string, string, string]>(['', '', ''])
    
    fetchText(`${apiHost}/api/show/lvgmc-forecast/Latvija_LTV_pilsetas_tekosa_dn.csv`)
        .then(csv => {
            const csvLines = csv.split('\n')
            console.log(csvLines)
            const citiesOffset = 4
            const cityData: CityData[] = csvCityListInOrder
                .map((city, i): CityData => {
                    const parts = csvLines[i + citiesOffset].split(';')
                    const temperature = Number(parts[1])
                    const iconCode = Number(parts[2])
                    const icon = codeToIcon(iconCode)
                    const coord = cityCoords[city]
                    if (!coord) throw new Error(`No coord found for city: ${city}`)
                    return [city, coord, temperature, icon]
                })
                .filter(([city]) => citiesToShow.includes(city))
            
            const windOffset = 30
            const windParts = csvLines[windOffset].split(';')
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
        img.src = bgMap
    }

    return <>
        LvgmcForecast
        <br/>
        <canvas
            ref={c =>canvas=c}
            width={'3840px'}
            height={'1440px'}
            style={{ 'width': '1000px', 'height': '374px' }}
        />
    </>
}
