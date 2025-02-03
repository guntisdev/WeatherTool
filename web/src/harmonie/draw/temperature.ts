import { valueToColorInterpolated, valueToColorThreshold } from '../../helpers/interpolateColors'
import { MeteoConversion } from '../../interfaces/interfaces'
import { TEMPERATURES } from './constants'


export function temperatureColors(
    encodedValue: number,
    { reference, binaryScale, decimalScale}: MeteoConversion,
    isInterpolated = true,
): [number, number, number, number] {
    const temperatureC = (reference + encodedValue * Math.pow(2, binaryScale)) * Math.pow(10, -decimalScale) - 273.15

    return isInterpolated
        ? valueToColorInterpolated(temperatureC, TEMPERATURES)
        : valueToColorThreshold(temperatureC, TEMPERATURES)
}
