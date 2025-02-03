type RGBAu8 = [number, number, number, number]

const FOG: RGBAu8 = [254, 248, 0, 255] // 'fef800'
const FOG_FREEZING: RGBAu8 = [194, 188, 0, 255] // 'c2bc00'
const RAIN_LIGHT: RGBAu8 = [0, 255, 0, 255] // '00ff00'
const RAIN_MODERATE: RGBAu8 = [0, 197, 0, 255] // '00c500'
const RAIN_HEAVY: RGBAu8 = [0, 132, 0, 255] // '008400'
const ICE_LIGHT: RGBAu8 = [255, 32, 55, 255] // 'ff2037'
const ICE_HEAVY: RGBAu8 = [220, 0, 0, 255] // 'dc0000'
export function categoricalRainColors(value: number): RGBAu8 {
    switch (value) {
        case 0:
            return FOG
        case 1*32:
            return FOG_FREEZING
        case 2*32:
            return RAIN_LIGHT
        case 3*32:
            return RAIN_MODERATE
        case 4*32:
            return RAIN_HEAVY
        case 5*32:
            return ICE_LIGHT
        case 6*32:
            return ICE_HEAVY
        default:
            return [255, 255, 255, 0]
    }
}

/* FROM CHATGPT
0 = No rain
1 * 32 = 32 (Drizzle)
2 * 32 = 64 (Light rain)
3 * 32 = 96 (Moderate rain)
4 * 32 = 128 (Heavy rain)
5 * 32 = 160 (Very heavy rain, if applicable)
6 * 32 = 192 (Extreme rain, if applicable)
*/

export function hexToU8(hex: string): [number, number, number] {
    return [parseInt('0x'+hex.slice(0, 2)), parseInt('0x'+hex.slice(2, 4)), parseInt('0x'+hex.slice(4, 6))]
}
