type RGBAu8 = [number, number, number, number]

const DRIZZLE: RGBAu8 = [5, 200, 0, 255]
const RAIN: RGBAu8 = [50, 150, 0, 255]
const SLEET: RGBAu8 = [255, 175, 0, 255]
const SNOW: RGBAu8 = [0, 160, 255, 255]
const FREEZING_DRIZZLE: RGBAu8 = [255, 100, 120, 255]
const FREEZING_RAIN: RGBAu8 = [255, 0, 0, 255]
const GRAUPEL: RGBAu8 = [230, 40, 250, 255]
const HAIL: RGBAu8 = [180, 0, 250, 255]

export function categoricalRainColors(value: number): RGBAu8 {
    switch (value) {
        case 0:
            return DRIZZLE
        case 1*32:
            return RAIN
        case 2*32:
            return SLEET
        case 3*32:
            return SNOW
        case 4*32:
            return FREEZING_DRIZZLE
        case 5*32:
            return FREEZING_RAIN
        case 6*32:
            return GRAUPEL
        case 7*32:
            return HAIL
        default:
            return [255, 255, 255, 0]
    }
}

export function hexToU8(hex: string): [number, number, number] {
    return [parseInt('0x'+hex.slice(0, 2)), parseInt('0x'+hex.slice(2, 4)), parseInt('0x'+hex.slice(4, 6))]
}
