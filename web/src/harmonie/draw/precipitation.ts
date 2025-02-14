import moment from 'moment'
import { valueToColorInterpolated, valueToColorThreshold } from '../../helpers/interpolateColors'
import { GribMessage, MeteoConversion } from '../interfaces'
import { PRECIPITATION } from './constants'
import { fetchBuffer } from '../../helpers/fetch'
import { apiHost } from '../../consts'


export function precipitationColors(
    encodedValue: number,
    { reference, binaryScale, decimalScale}: MeteoConversion,
    isInterpolated = true,
): [number, number, number, number] {
    const rainMM = (reference + encodedValue * Math.pow(2, binaryScale)) * Math.pow(10, -decimalScale)

    return isInterpolated
        ? valueToColorInterpolated(rainMM, PRECIPITATION)
        : valueToColorThreshold(rainMM, PRECIPITATION)
}

export function hourPrecipitationColors(
    encodedValNow: number,
    convNow: MeteoConversion,
    encodedValPrev: number,
    convPrev: MeteoConversion,
    isInterpolated = true,
): [number, number, number, number] {
    const nowVal = (convNow.reference + encodedValNow * Math.pow(2, convNow.binaryScale)) * Math.pow(10, -convNow.decimalScale)
    const prevVal = (convPrev.reference + encodedValPrev * Math.pow(2, convPrev.binaryScale)) * Math.pow(10, -convPrev.decimalScale)

    const rainMM = nowVal - prevVal
    return isInterpolated
        ? valueToColorInterpolated(rainMM, PRECIPITATION)
        : valueToColorThreshold(rainMM, PRECIPITATION)
}

export function fetchHourPrecipitationData(
    customMessage: GribMessage,
    gribArr: GribMessage[],
): Promise<[GribMessage[], ArrayBuffer[], ArrayBuffer[]]> {
    const totalPrecipitation = gribArr.find(g => isPrecipitation(g)
        && g.time.referenceTime === customMessage.time.referenceTime
        && g.time.forecastTime === customMessage.time.forecastTime
    )
    if (!totalPrecipitation) throw new Error('Not found total precipitation')

    const { forecastTime } = totalPrecipitation.time
    const prevForecastTime = moment(forecastTime.replace(/(\d{2})(\d{2})Z/, '$1:$2:00Z'))
        .subtract(1, 'hours')
        .utc()
        .format("YYYY-MM-DDTHHmm")+'Z'

    const prevTotalPrecipitation = gribArr.find(g => isPrecipitation(g)
        && g.time.referenceTime === customMessage.time.referenceTime
        && g.time.forecastTime === prevForecastTime
    )
    
    const section7now = totalPrecipitation.sections.find(section => section.id === 7)
    const section7prev = prevTotalPrecipitation?.sections.find(section => section.id === 7)
    if (!section7now) throw new Error('Didnt found binary section for total precipitation')
    
    const nowBinaryOffset = section7now.offset + 5
    const nowBinaryLength = section7now.size - 5
    const nowFileName = `harmonie_${totalPrecipitation.time.referenceTime}_${totalPrecipitation.time.forecastTime}.grib`

    // for oldest message there is no more -1h message
    let prevPromise: Promise<ArrayBuffer> | undefined
    if (prevTotalPrecipitation && section7prev) {
        const prevBinaryOffset = section7prev.offset + 5
        const prevBinaryLength = section7prev.size - 5
        const prevFileName = `harmonie_${prevTotalPrecipitation.time.referenceTime}_${prevTotalPrecipitation.time.forecastTime}.grib`
        prevPromise = fetchBuffer(`${apiHost}/api/grib/binary-chunk/${prevBinaryOffset}/${prevBinaryLength}/${prevFileName}`)
    }
    if (!prevPromise) prevPromise = new Promise(resolve => resolve(new Uint8Array(nowBinaryLength).buffer))

    return Promise.all([
            fetchBuffer(`${apiHost}/api/grib/binary-chunk/${nowBinaryOffset}/${nowBinaryLength}/${nowFileName}`),
            prevPromise,
        ]).then(([bufferNow, bufferPrev]) => {
            const messages = [customMessage, totalPrecipitation, prevTotalPrecipitation ?? totalPrecipitation]
            const buffers = [bufferNow, bufferNow, bufferPrev]
            return [messages, buffers, []]
        })
}

export function isPrecipitation(grib: GribMessage): boolean {
    return grib.meteo.discipline === 0 && grib.meteo.category === 1 && grib.meteo.product === 52
}

export function isCalculatedHourPrecipitation(grib: GribMessage): boolean {
    return grib.meteo.discipline === 0 && grib.meteo.category === 1 && grib.meteo.product === 236
}

export function getFakeHourPrecipitation(totalPrecipitation: GribMessage): GribMessage {
    const modifiedPrecipitation = structuredClone(totalPrecipitation)
    modifiedPrecipitation.meteo = {...modifiedPrecipitation.meteo, product: 236}
    modifiedPrecipitation.title = 'meteorology, moisture, hour precipitation rate'
    return modifiedPrecipitation
}