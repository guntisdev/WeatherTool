import { apiHost } from '../../consts'
import { fetchBuffer } from '../../helpers/fetch'
import { valueToColorInterpolated } from '../../helpers/interpolateColors'
import { GribMessage, MeteoConversion } from '../interfaces'
import { WIND_SPEED } from './constants'
import { rotateWind } from './windRotate'

const CELL_SIZE = 12

export function windDirectionArrows(
    imgData: ImageData,
    messages: GribMessage[],
    buffers: Uint8Array[],
) {
    const [, metaU, metaV] = messages
    const { conversion: convU } = metaU
    const { conversion: convV } = metaV
    const [, bufferU, bufferV] = buffers
    const cols = imgData.width
    const rows = imgData.height
    const canvas = document.createElement('canvas')
    canvas.width = imgData.width
    canvas.height = imgData.height
    const ctx = canvas.getContext('2d')!
    ctx.putImageData(imgData, 0, 0) // draw wind speed color below direction
    const directions: number[][] = []
    const bytesPerPoint = metaU.bitsPerDataPoint/8

    const lambert = (messages[0].grid as any).lambert
    let rot_lat = lambert[0] / 1_000_000
    let rot_lon = lambert[1] / 1_000_000
    // rot_lat = -9999.0
    // rot_lon = -9999.0
    let reg_lat = 56.530592
    let reg_lon = -2.918742

    for (let row = 0; row < rows; row++) {
        directions[row] = []
        for (let col = 0; col < cols; col++) {
            const bufferI = (row * cols + col) * bytesPerPoint
    
            const encodedU = toInt(bufferU.slice(bufferI, bufferI+bytesPerPoint))
            const encodedV = toInt(bufferV.slice(bufferI, bufferI+bytesPerPoint))
            const windSpeedU = (convU.reference + encodedU * Math.pow(2, convU.binaryScale)) * Math.pow(10, -convU.decimalScale)
            const windSpeedV = (convV.reference + encodedV * Math.pow(2, convV.binaryScale)) * Math.pow(10, -convV.decimalScale)

            // const directionDeg = (Math.atan2(windSpeedU, windSpeedV)*180/Math.PI + 360 +45) % 360
            const directionDeg = rotateWind(rot_lat, rot_lon, reg_lat, reg_lon, windSpeedU, windSpeedV)[0]
            const directionRad = (Math.PI / 180) * directionDeg

            directions[row][col] = directionRad
        }
    }

    const gridH = Math.floor(directions.length/CELL_SIZE)
    const gridW = Math.floor(directions[0].length/CELL_SIZE)
    for (let row = 0; row < gridH; row++) {
        for (let col = 0; col < gridW; col++) {
            const directionAvg = true
                ? getAvgDirection(directions, row, col)
                : getDirection(directions, row, col)

            const centerX = col * CELL_SIZE + CELL_SIZE/2
            const centerY = row * CELL_SIZE + CELL_SIZE/2
            const arrowLength = CELL_SIZE*0.9

            ctx.save()
            ctx.translate(centerX, centerY)
            ctx.rotate(directionAvg)

            ctx.beginPath();
            ctx.moveTo(-arrowLength / 2, 0)
            ctx.lineTo(arrowLength / 2, 0)
            ctx.stroke()

            const arrowheadSize = CELL_SIZE/4
            ctx.beginPath()
            ctx.moveTo(arrowLength / 2, 0)
            ctx.lineTo(arrowLength / 2 - arrowheadSize, -arrowheadSize / 2)
            ctx.lineTo(arrowLength / 2 - arrowheadSize, arrowheadSize / 2)
            ctx.closePath()
            ctx.fill()

            ctx.restore()
        }
    }

    return ctx.getImageData(0, 0, imgData.width, imgData.height)
}

function getDirection(directions: number[][], gridRow: number, gridCol: number) {
    const directionRow = gridRow * CELL_SIZE + Math.round(CELL_SIZE/2)
    const directionCol = gridCol * CELL_SIZE + Math.round(CELL_SIZE/2)
    return directions[directionRow][directionCol]
}

// in radians
function getAvgDirection(directions: number[][], gridRow: number, gridCol: number) {
    let sumSin = 0; // Sum of sine components
    let sumCos = 0; // Sum of cosine components

    for (let row = 0; row < CELL_SIZE; row++) {
        for (let col = 0; col < CELL_SIZE; col++) {
            const directionRow = gridRow * CELL_SIZE + row;
            const directionCol = gridCol * CELL_SIZE + col;

            const directionRad = directions[directionRow][directionCol]

            sumSin += Math.sin(directionRad);
            sumCos += Math.cos(directionRad);
        }
    }

    // Compute the average direction
    const avgDirection = Math.atan2(sumSin, sumCos); // Result is in radians
    return (avgDirection + 2 * Math.PI) % (2 * Math.PI); // Ensure the result is in [0, 2π)
}

// actually calculates and draws wind speed
export function windDirectionColors(
    encodedU: number,
    encodedV: number,
    convU: MeteoConversion,
    convV: MeteoConversion,
) {
    const windSpeedU = (convU.reference + encodedU * Math.pow(2, convU.binaryScale)) * Math.pow(10, -convU.decimalScale)
    const windSpeedV = (convV.reference + encodedV * Math.pow(2, convV.binaryScale)) * Math.pow(10, -convV.decimalScale)
    const windSpeed = Math.sqrt(Math.pow(windSpeedU, 2) + Math.pow(windSpeedV, 2))

    return valueToColorInterpolated(windSpeed, WIND_SPEED)
}

export function windSpeedColors(
    encodedValue: number,
    { reference, binaryScale, decimalScale}: MeteoConversion,
) {
    const windSpeed = (reference + encodedValue * Math.pow(2, binaryScale)) * Math.pow(10, -decimalScale)
    return valueToColorInterpolated(windSpeed, WIND_SPEED)
}

export function fetchWindData(
    customMessage: GribMessage,
    gribArr: GribMessage[],
    fileName: string,
): Promise<[GribMessage[], ArrayBuffer[], ArrayBuffer[]]> {
    const windU = gribArr.find(m => m.meteo.discipline===0 && m.meteo.category===2 && m.meteo.product===2 && m.meteo.levelType===103 && m.meteo.levelValue===10)
    const windV = gribArr.find(m => m.meteo.discipline===0 && m.meteo.category===2 && m.meteo.product===3 && m.meteo.levelType===103 && m.meteo.levelValue===10)
    if (!windU || !windV) throw new Error('Didnt found u/v components of wind')

    const section7u = windU.sections.find(section => section.id === 7)
    const section7v = windV.sections.find(section => section.id === 7)
    if (!section7u || !section7v) throw new Error('Didnt found binary section for wind u/v')

    const uBinaryOffset = section7u.offset + 5
    const uBinaryLength = section7u.size - 5

    const vBinaryOffset = section7v.offset + 5
    const vBinaryLength = section7v.size - 5

    return Promise.all([
        fetchBuffer(`${apiHost}/api/grib/binary-chunk/${uBinaryOffset}/${uBinaryLength}/${fileName}`),
        fetchBuffer(`${apiHost}/api/grib/binary-chunk/${vBinaryOffset}/${vBinaryLength}/${fileName}`),
    ]).then(([bufferU, bufferV]) => {
        const messages = [customMessage, windU, windV]
        const buffers = [bufferU, bufferU, bufferV]
        return [messages, buffers, []]
    })
}

function toInt(bytes: Uint8Array): number {
    return bytes.reduce((acc, curr) => acc * 256 + curr)
}

export function isWindSpeed(grib: GribMessage): boolean {
    return grib.meteo.discipline === 0 && grib.meteo.category === 2 && grib.meteo.product === 1
}

export function isCalculatedWindDirection(grib: GribMessage): boolean {
    return grib.meteo.discipline === 0 && grib.meteo.category === 2 && grib.meteo.product === 192
}

export function getFakeWindDirection(windSpeed: GribMessage): GribMessage {
    const modifiedWindSpeed = structuredClone(windSpeed)
    modifiedWindSpeed.meteo = {...modifiedWindSpeed.meteo, product: 192}
    modifiedWindSpeed.title = 'meteorology, momentum, wind direction 10m (calc u,v)'
    return modifiedWindSpeed
}
