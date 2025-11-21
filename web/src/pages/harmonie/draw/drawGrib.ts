import { interpolateColors } from '../../../helpers/interpolateColors'
import { CropBounds, GribMessage, MeteoParam } from '../interfaces'
import { applyBitmask } from './bitmask'
import { extractFromBounds } from './bounds'
import { categoricalRainColors } from './categoricalRain'
import { hourPrecipitationColors, precipitationColors } from './precipitation'
import { temperatureColors } from './temperature'
import { isCalculatedWindDirection, windDirectionArrows, windDirectionColors, windSpeedColors } from './windDirection'

import latvia_border from '../../../assets/latvia_contour.webp'
import { snowDepthColors } from './snowDepth'
const latviaBoderImg = new Image()
latviaBoderImg.onload = () => {}
latviaBoderImg.src = latvia_border

/*
* final cropped size should be 1365x576px - divided by 3 (455x192) or 3.5 (390x165)
* image should be rotade 26 degrees
* currently image is 400x300px 
*/
export function drawGrib(
    canvas: HTMLCanvasElement,
    messages: GribMessage[],
    buffers: Uint8Array[],
    bitmasks: Uint8Array[],
    cropBounds: CropBounds | undefined,
    isContour: boolean,
    isInterpolated: boolean,
): void {
    // normally we have one message/buffer/bitmask?. special cases have multiple like wind direction
    const [grib] = messages

    let { grid } = grib
    let { cols, rows } = grid

    let modifiedBuffers = buffers.map((buffer, i) => {
        const bytesPerPoint = messages[i].bitsPerDataPoint / 8
        return bitmasks[i] ? applyBitmask(grid, buffer, bitmasks[i], bytesPerPoint) : buffer
    })

    if (cropBounds) {
        modifiedBuffers = modifiedBuffers.map(buffer => extractFromBounds(grib, buffer, cropBounds))
        cols = cropBounds.width
        rows = cropBounds.height
    }

    canvas.width = cols
    canvas.height = rows
    // canvas.style.width = '100%'
    // canvas.style.minWidth = '1365px'
    // canvas.style.border = '1px solid red'
    const ctx = canvas.getContext('2d')!
    let imgData = ctx.createImageData(cols, rows)
    
    fillImageData(imgData, messages, modifiedBuffers, isInterpolated)
    ctx.putImageData(imgData, 0, 0)
    flipCanvasV(canvas, ctx)

    if (cropBounds) {
        drawRotate(canvas, ctx, cropBounds.angle, isInterpolated, 3.5)
    }

    if (isCalculatedWindDirection(grib)) {
        const directionArrows = windDirectionArrows(messages, modifiedBuffers, cols, rows, cropBounds)
        ctx.drawImage(directionArrows, 0, 0)
    }

    if (isContour && cropBounds) {
        drawContour(canvas, ctx) // draw latvia contour only on cropped image
    }
}

const CATEGORICAL_RAIN = [0, 1, 192]
const TOTAL_PRECIPITATION = [0, 1, 52]
const HOUR_PRECIPITATION = [0, 1, 236]
const RAIN_PRECIPITATION = [0, 1, 65]
const SNOW_DEPTH = [0, 1, 60]
const TEMPERATURE = [0, 0, 0]
const WIND_DIRECTION = [0, 2, 192]
const WIND_SPEED = [0, 2, 1]
const WIND_SPEED_GUST = [0, 2, 22]

function fillImageData(
    imgData: ImageData,
    messages: GribMessage[],
    buffers: Uint8Array[],
    isInterpolated: boolean,
) {
    const [grib] = messages
    const [buffer] = buffers
    const colors: [string, string] = ['#0000ff', '#ffff00']

    const { meteo, conversion, bitsPerDataPoint } = grib
    const bytesPerPoint = grib.bitsPerDataPoint / 8
    const fromColor = rgbHexToU8(colors[0])
    const toColor = rgbHexToU8(colors[1])

    const cols = imgData.width
    const rows = imgData.height

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {

            const bufferI = (row * cols + col) * bytesPerPoint
            const index = (row * cols + col) * 4
            const firstByte = buffer[bufferI]
            const encodedValue = toInt(buffer.slice(bufferI, bufferI+bitsPerDataPoint/8))

            let color = [255, 255, 255, 255]
            if (isMeteoEqual(meteo, CATEGORICAL_RAIN)) {
                color = categoricalRainColors(firstByte)
            }
            else if (isMeteoEqual(meteo, TOTAL_PRECIPITATION)) {
                color = precipitationColors(encodedValue, conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, HOUR_PRECIPITATION)) {
                const [, nowPrec, prevPrec] = buffers
                const encodedValNow = toInt(nowPrec.slice(bufferI, bufferI+bitsPerDataPoint/8))
                const encodedValPrev = toInt(prevPrec.slice(bufferI, bufferI+bitsPerDataPoint/8))
                const [, metaNow, metaPrev] = messages
                color = hourPrecipitationColors(encodedValNow, metaNow.conversion, encodedValPrev, metaPrev.conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, RAIN_PRECIPITATION)) {
                color = precipitationColors(encodedValue, conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, SNOW_DEPTH)) {
                color = snowDepthColors(encodedValue, conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, TEMPERATURE)) {
                color = temperatureColors(encodedValue, conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, WIND_DIRECTION)) {
                const [, bufferU, bufferV] = buffers
                const encodedValU = toInt(bufferU.slice(bufferI, bufferI+bitsPerDataPoint/8))
                const encodedValV = toInt(bufferV.slice(bufferI, bufferI+bitsPerDataPoint/8))
                const [, metaU, metaV] = messages // first message fake one 0-2-192
                color = windDirectionColors(encodedValU, encodedValV, metaU!.conversion, metaV!.conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, WIND_SPEED)) {
                color = windSpeedColors(encodedValue, conversion, isInterpolated)
            }
            else if (isMeteoEqual(meteo, WIND_SPEED_GUST)) {
                color = windSpeedColors(encodedValue, conversion, isInterpolated)
            }
            else {
                color = interpolateColors(firstByte, fromColor, toColor)
            }

            imgData.data[index] = color[0]
            imgData.data[index + 1] = color[1]
            imgData.data[index + 2] = color[2]
            imgData.data[index + 3] = color[3]
        }
    }
}

export function flipCanvasV(
    canvas: HTMLCanvasElement,
    ctx: CanvasRenderingContext2D,
) {
    const tmpCanvas = document.createElement('canvas')!
    const tmpCtx = tmpCanvas.getContext('2d')!
    tmpCanvas.width = canvas.width
    tmpCanvas.height = canvas.height

    tmpCtx.save()
    tmpCtx.scale(1, -1)
    tmpCtx.drawImage(canvas, 0, -canvas.height)
    tmpCtx.restore()

    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.drawImage(tmpCanvas, 0, 0)
}

export function drawRotate(
    canvas: HTMLCanvasElement,
    ctx: CanvasRenderingContext2D,
    angleDegrees: number,
    isInterpolated = false,
    scale = 1,
) {
    const tempCanvas = document.createElement('canvas')
    tempCanvas.width = canvas.width
    tempCanvas.height = canvas.height
    const tempCtx = tempCanvas.getContext('2d')!
    tempCtx.save()
    tempCtx.translate(tempCanvas.width/2, tempCanvas.height/2)
    tempCtx.rotate(angleDegrees * Math.PI/180)
    tempCtx.drawImage(canvas, -canvas.width/2, -canvas.height/2)
    tempCtx.restore()

    ctx.clearRect(0, 0, canvas.width, canvas.height)
    canvas.width = 640*4 - 1000 // -offset right
    canvas.height = 540*1.7 + 140
    // canvas.style.border = "1px solid red"
    // console.log(ctx.imageSmoothingEnabled, ctx.imageSmoothingQuality)
    if (isInterpolated) {
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high' // Options: 'low', 'medium', 'high'
    } else {
        ctx.imageSmoothingEnabled = false
    }
    ctx.save()
    ctx.translate(canvas.width/2+255, canvas.height/2)
    ctx.scale(scale, scale)
    ctx.drawImage(tempCanvas, -tempCanvas.width/1.5, -tempCanvas.height/1.5)
    ctx.restore()
}

function drawContour(
    canvas: HTMLCanvasElement,
    ctx: CanvasRenderingContext2D,
): void {
    ctx.save()
    ctx.translate(canvas.width/2 -20+255, canvas.height/2 -10)
    // TODO create contour image exact scale when sizes will be accepted
    const scale = 5.3/3.5
    const scaledWidth = latviaBoderImg.width/scale
    const scaledHeight = latviaBoderImg.height/scale
    ctx.drawImage(latviaBoderImg, 
        -scaledWidth/2, -scaledHeight/2,
        scaledWidth, scaledHeight
    )
    ctx.restore()
}


function rgbHexToU8(hex: string): RGBAu8 {
    return [
        parseInt(`0x${hex.slice(1, 3)}`),
        parseInt(`0x${hex.slice(3, 5)}`),
        parseInt(`0x${hex.slice(5, 7)}`),
        255,
    ]
}

type RGBAu8 = [number, number, number, number]

export function isMeteoEqual(meteo: MeteoParam, arr: number[]): boolean {
    const arr2 = [meteo.discipline, meteo.category, meteo.product]
    return arr.length === arr2.length && arr.every((value, index) => value === arr2[index])
}

function toInt(bytes: Uint8Array): number {
    return bytes.reduce((acc, curr) => acc * 256 + curr)
}

export function toSignedInt(bytes: Uint8Array): number {
    const unsigned = toInt(bytes)

    const signBit = 1 << (bytes.length * 8 - 1) // Example: 16-bit -> 0x8000
    if (unsigned & signBit) {
        // If the sign bit is set, compute the two's complement
        return unsigned - (1 << (bytes.length * 8))
    }

    return unsigned // If the sign bit is not set, return as is
}
