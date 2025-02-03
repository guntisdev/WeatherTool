export type ColorEntry = { value: number, color: [number, number, number, number]}

export const WIND_SPEED: ColorEntry[] = [
    { value: 40, color: [170, 0, 190, 255] },
    { value: 35, color: [170, 0, 190, 255] },
    { value: 30, color: [225, 20, 0, 255] },
    { value: 25, color: [225, 20, 0, 255] },
    { value: 20, color: [255, 160, 0, 255] },
    { value: 15, color: [255, 250, 170, 255] },
    { value: 10, color: [80, 240, 80, 255] },
    { value: 5, color: [45, 155, 150, 255] },
    { value: 0, color: [180, 240, 250, 255] },
]

export const PRECIPITATION: ColorEntry[] = [
    { value: 30, color: [126, 26, 99, 255] },
    { value: 25, color: [120, 27, 131, 255] },
    { value: 20, color: [84, 20, 130, 255] },
    { value: 15, color: [49, 16, 129, 255] },
    { value: 10, color: [9, 15, 129, 255] },
    { value: 6, color: [0, 24, 150, 255] },
    { value: 4, color: [0, 43, 186, 255] },
    { value: 2, color: [10, 70, 220, 255] },
    { value: 1, color: [40, 109, 246, 255] },
    { value: 0.5, color: [80, 162, 248, 255] },
    { value: 0.2, color: [118, 202, 249, 255] },
    { value: 0.1, color: [152, 233, 252, 255] },
    { value: 0.05, color: [255, 255, 255, 255] },
]

// from ltv
export const TEMPERATURES: ColorEntry[] = [
    { value: 35, color: [155, 30, 30, 255] },
    { value: 10, color: [250, 225, 5, 255] },
    { value: 0, color: [80, 190, 240, 255] },
    { value: -15, color: [30, 70, 155, 255] },
    { value: -30, color: [140, 30, 190, 255] },
]

// from yr.no
// export const TEMPERATURES: ColorEntry[] = [
//     { value: 50, color: [133, 0, 62, 255] },
//     { value: 40, color: [195, 0, 0, 255] },
//     { value: 30, color: [255, 76, 56, 255] },
//     { value: 20, color: [255, 175, 111, 255] },
//     { value: 10, color: [255, 243, 81, 255] },
//     { value: 0, color: [195, 246, 215, 255] },
//     { value: -10, color: [94, 231, 240, 255] },
//     { value: -20, color: [63, 201, 243, 255] },
//     { value: -30, color: [79, 157, 232, 255] },
//     { value: -40, color: [0, 81, 163, 255] },
//     { value: -50, color: [79, 15, 134, 255] },
// ]
