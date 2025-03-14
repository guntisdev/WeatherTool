import { Component } from 'solid-js'
import { fetchText } from '../../helpers/fetch'
import { apiHost } from '../../consts'
import { codeToIcon } from '../weatherIcons/iconConsts'
import rigaBgUrl from '../../assets/bg-riga.webp'
import arrowUrl from '../../assets/arrow.webp'
import { drawRotatedImage, getAngleFromString } from '../map/windAngles'

type TableWeather = {
    hour: string,
    temperature: string,
    windDirection: string,
    windSpeed: string,
    icon: string,
}

export const Table: Component<{
    csvUrl: string,
}> = ({
    csvUrl,
}) => {
    let canvas: HTMLCanvasElement | undefined
    const rigaBg = new Image()
    rigaBg.src = rigaBgUrl
    const arrowImg = new Image()
    arrowImg.src = arrowUrl

    fetchText(`${apiHost}/api/show/lvgmc-forecast/${csvUrl}`)
        .then(csv => {
            const csvLines = csv.split('\n')
            const dateStr = csvLines[2].split(';')[0].split('.').reverse().join('.')
            const weekDay = new Date(dateStr).getDay()
            const weekDayLV = weekDaysLV.get(weekDay)?.toUpperCase() ?? ''
            const dayOrNight = csvLines[2].split(';')[1]
            const rigaOffset = dayOrNight === 'DIENA' ? 24 : 51
            const data: TableWeather[] = csvLines
                .slice(rigaOffset, rigaOffset+4)
                .map((line): TableWeather => {
                    const parts = line.split(';')
                    const hour = parts[0].slice(6, 11).replace(':', '.')
                    const temperature = parts[1]
                    const windDirection = parts[3]
                    const windSpeed = parts[5]
                    const iconCode = Number(parts[7])
                    const icon = codeToIcon(iconCode)

                    return { hour, temperature, windDirection, windSpeed, icon }
                })
            drawTable(canvas!, rigaBg, arrowImg, data, weekDayLV)
        })

    return <>
        <canvas ref={canvas} width='1040px' height='530px' />
    </>
}


function drawTable(
    canvas: HTMLCanvasElement,
    rigaBg: HTMLImageElement,
    arrowImg: HTMLImageElement,
    data: TableWeather[],
    title: string,
) {
    if (!data.length) throw new Error('Empty data for drawing table!')
    canvas.width = rigaBg.width
    canvas.height = rigaBg.height
    const ctx = canvas.getContext('2d')!
    ctx.drawImage(rigaBg, 0, 0)

    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.font = 'bold 30px Rubik'
    ctx.fillStyle = '#000000'
    ctx.letterSpacing = '10px'
    ctx.fillText(title, rigaBg.width/2, rigaBg.height*0.065)

    ctx.letterSpacing = '0px'
    const colWidth = rigaBg.width / data.length
    data.forEach((weather, i) => {
        const midX = colWidth * (i + 0.5)
        ctx.textAlign = 'center'
        ctx.font = 'bold 20px Rubik'
        ctx.fillStyle = '#FFFFFF'
        ctx.fillText(weather.hour, midX, rigaBg.height * 0.2)

        ctx.font = `normal 240px Daira by LTV grafika`
        ctx.fillText(weather.icon, midX, rigaBg.height * 0.5)

        ctx.font = 'normal 50px Rubik'
        ctx.fillText(weather.temperature, midX, rigaBg.height * 0.75)

        const bottomLineY = rigaBg.height * 0.9
        ctx.font = 'bold 30px Rubik'
        ctx.textAlign = 'center'
        ctx.fillText(weather.windDirection, midX, bottomLineY)
        const dirWidth = ctx.measureText(weather.windDirection).width
        const windAngle = getAngleFromString(weather.windDirection)
        ctx.textAlign = 'right'
        drawRotatedImage(ctx, arrowImg, midX-dirWidth/2-arrowImg.width-10, bottomLineY-arrowImg.height/2, windAngle)
        ctx.textAlign = 'left'
        ctx.fillText(weather.windSpeed, midX+dirWidth/2+10, bottomLineY)
    })
}

const weekDaysLV = new Map([
    [1, 'pirmdiena'],
    [2, 'otrdiena'],
    [3, 'trešdiena'],
    [4, 'ceturtdiena'],
    [5, 'piektdiena'],
    [6, 'sestdiena'],
    [7, 'svētdiena'],
])