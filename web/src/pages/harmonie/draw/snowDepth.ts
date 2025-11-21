import { valueToColorInterpolated, valueToColorThreshold } from '../../../helpers/interpolateColors'
import { MeteoConversion } from '../interfaces'
import { SNOW_DEPTH } from './constants'

export function snowDepthColors(
    encodedValue: number,
    { reference, binaryScale, decimalScale}: MeteoConversion,
    isInterpolated = true,
): [number, number, number, number] {
    const rainMM = (reference + encodedValue * Math.pow(2, binaryScale)) * Math.pow(10, -decimalScale)

    return isInterpolated
        ? valueToColorInterpolated(rainMM, SNOW_DEPTH)
        : valueToColorThreshold(rainMM, SNOW_DEPTH)
}
