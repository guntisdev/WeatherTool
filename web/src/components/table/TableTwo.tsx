import { Accessor, Component, createEffect } from 'solid-js'
import { codeToIcon } from '../weatherIcons/iconConsts'
import table2BgUrl from '../../assets/bg-table2-left.webp'
import arrowUrl from '../../assets/arrow.webp'
import { drawRotatedImage, getAngleFromString } from '../map/windAngles'

type TableWeather = {
    temperature: string,
    windDirection: string,
    windSpeed: string,
    icon: string,
}

export const TableTwo: Component<{
    getCsvLines: Accessor<string[]>,
}> = ({
    getCsvLines,
}) => {
    let canvas: HTMLCanvasElement | undefined
    const table2Bg = new Image()
    table2Bg.src = table2BgUrl
    const arrowImg = new Image()
    arrowImg.src = arrowUrl

    createEffect(() => {
        const csvLines = getCsvLines()
        if (csvLines.length <= 0) return;
        const dateStr = csvLines[0].split(';')[0].split('.').reverse().join('.')
        const weekDay = new Date(dateStr).getDay()
        const weekDayLV = weekDaysLV.get(weekDay)?.toUpperCase() ?? ''

        const tempParts = csvLines[2].split(';')
        const iconParts = csvLines[3].split(';')
        const windDirParts = csvLines[4].split(';')
        const windSpeedParts = csvLines[5].split(';')

        const data: TableWeather[] = [
            {
                temperature: tempParts[2],
                windDirection: windDirParts[2],
                windSpeed: windSpeedParts[2],
                icon: codeToIcon(Number(iconParts[2])),
            },
            {
                temperature: tempParts[5],
                windDirection: windDirParts[5],
                windSpeed: windSpeedParts[5],
                icon: codeToIcon(Number(iconParts[5])),
            },
        ]

        drawTable(canvas!, table2Bg, arrowImg, data, weekDayLV)
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
        ctx.fillStyle = '#FFFFFF'

        ctx.font = `normal 240px Daira by LTV grafika`
        ctx.fillText(weather.icon, midX, rigaBg.height * 0.4)

        ctx.font = 'normal 50px Rubik'
        ctx.fillText(weather.temperature, midX, rigaBg.height * 0.7)

        const bottomLineY = rigaBg.height * 0.85
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
    [0, 'svētdiena'],
    [1, 'pirmdiena'],
    [2, 'otrdiena'],
    [3, 'trešdiena'],
    [4, 'ceturtdiena'],
    [5, 'piektdiena'],
    [6, 'sestdiena'],
])