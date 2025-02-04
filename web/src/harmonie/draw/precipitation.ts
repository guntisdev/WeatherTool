import { valueToColorInterpolated, valueToColorThreshold } from '../../helpers/interpolateColors'
import { MeteoConversion } from '../interfaces'
import { PRECIPITATION } from './constants'


export function precipitationColors(
    encodedValue: number,
    { reference, binaryScale, decimalScale}: MeteoConversion,
    isInterpolated = true,
): [number, number, number, number] {
    // const rainMM = (reference + encodedValue * Math.pow(2, binaryScale)) * Math.pow(10, -decimalScale) / 1000
    const rainMM = (reference + encodedValue * Math.pow(2, binaryScale)) * Math.pow(10, -decimalScale)

    return isInterpolated
        ? valueToColorInterpolated(rainMM, PRECIPITATION)
        : valueToColorThreshold(rainMM, PRECIPITATION)
}
