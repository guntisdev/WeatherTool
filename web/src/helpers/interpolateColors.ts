import { ColorEntry } from '../harmonie/draw/constants'

type RGBu8 = [number, number, number]
type RGBAu8 = [number, number, number, number]

export function interpolateColors(value: number, a: RGBu8, b: RGBu8): RGBAu8 {
    const color = a.slice(0).map((from, i) => {
        const to = b[i]
        const delta = (to - from) * (value/255)
        return from + delta
    })

    return [...color, 255] as RGBAu8
}

export function valueToColorThreshold(value: number, colorArr: ColorEntry[]): [number, number, number, number] {
    const entries = colorArr.filter(t => t.value <= value)

    return entries.length === 0 ? colorArr[colorArr.length-1].color : entries[0].color
}

export function valueToColorInterpolated(value: number, colorArr: ColorEntry[]): [number, number, number, number] {
    const minIdx = colorArr.length-1
    if (value >= colorArr[0].value) return colorArr[0].color
    if (value <= colorArr[minIdx].value) return colorArr[minIdx].color

    let closeMax: ColorEntry = colorArr[0]
    let closeMin: ColorEntry = colorArr[minIdx]
    for (let i=0; i<colorArr.length; i++) {
        const currentDeg = colorArr[i].value
        if (currentDeg >= value && currentDeg < closeMax.value) {
            closeMax = colorArr[i]
        }

        if (currentDeg <= value && currentDeg > closeMin.value) {
            closeMin = colorArr[i]
        }
    }

    const maxDeg = closeMax.color.slice(0, 3) as [number, number, number]
    const minDeg = closeMin.color.slice(0, 3) as [number, number, number]
    const scale = Math.abs(closeMax.value - closeMin.value)
    const delta = 255*(value - closeMin.value)/scale
    const rgba = interpolateColors(delta, minDeg, maxDeg)

    return rgba
}
