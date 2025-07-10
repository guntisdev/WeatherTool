import { Accessor } from 'solid-js'

export type MeteoParam = {
    discipline: number, // 0=meteo, 1=hydro, 2=land surface, 3=space products
    category: number, // 0=temperature, 1=moisture, 6=cloud, 19=atmospheric
    product: number,
    subType: string, // now or over time avg/sum
    levelType: number, // 102 - entire atmosphere, 103 - above ground, above sea level
    levelValue: number, // meters above ground/sea level
}

export type MeteoConversion = {
    reference: number, // float
    binaryScale: number, // int
    decimalScale: number, // int
}

export type MeteoGrid = {
    cols: number,
    rows: number,
    template: number, // 0 - regular lat/lon
}

export type GribMessage = {
    offset: number,
    size: number,
    version: number,
    title: string,
    meteo: MeteoParam,
    grid: MeteoGrid,
    time: GribTime,
    bitsPerDataPoint: number,
    subType: string,
    conversion: MeteoConversion,
    sections: GribSection[],
}

export type GribSection = {
    offset: number,
    size: number,
    id: number,
}

export type GribTime = {
    referenceTime: string,
    forecastTime: string,
}

export type DrawOptions = {
    getIsCrop: Accessor<boolean>,
    getIsContour: Accessor<boolean>,
    getIsInterpolated: Accessor<boolean>,
}

// export const CROP_BOUNDS = { x: 1906-1-440, y: 895, width: 440, height: 380, angle: 26 }
export const CROP_BOUNDS = { x: 1906-1-660, y: 840, width: 660, height: 620, angle: 26 }
export type CropBounds = typeof CROP_BOUNDS
