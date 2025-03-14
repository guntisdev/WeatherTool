import { apiHost } from '../../consts'
import { fetchBuffer, fetchJson } from '../../helpers/fetch'
import { getFakeHourPrecipitation, isPrecipitation } from './draw/precipitation'
import { getFakeWindDirection, isWindSpeed } from './draw/windDirection'
import { GribMessage } from './interfaces'
import { fetchWindData, isCalculatedWindDirection } from './draw/windDirection'
import { fetchHourPrecipitationData, isCalculatedHourPrecipitation } from './draw/precipitation'

export function fetchGribList(): Promise<string[]> {
    return fetchJson(`${apiHost}/api/show/grib-list`)
        .then((fileList: string[]) => {
            return fileList.sort((a: string, b: string) => a < b ? 1 : -1)
        })
}

export function fetchGribListStructure(): Promise<GribMessage[]> {
    return fetchJson(`${apiHost}/api/show/grib-all-structure`)
        .then((gribList: GribMessage[]) => {
            return [
                ...gribList,
                ...gribList.filter(isWindSpeed).map(getFakeWindDirection),
                ...gribList.filter(isPrecipitation).map(getFakeHourPrecipitation),
            ].sort((a, b) => a.title > b.title ? 1 : -1)
        })
}

export function fetchGribBinaries(
    grib: GribMessage,
    gribList: GribMessage[],
): Promise<[GribMessage[], Uint8Array[], Uint8Array[]]> {
    const fileName = `harmonie_${grib.time.referenceTime}_${grib.time.forecastTime}.grib`
    const bitmaskSection = grib.sections.find(section => section.id === 6)
    const binarySection = grib.sections.find(section => section.id === 7)
    if (!bitmaskSection || !binarySection) throw new Error('Grib does not have binary or bitmap section')

    const bitmaskOffset = bitmaskSection.offset + 6
    const bitmaskLength = bitmaskSection.size - 6
    const bitmaskPromise = bitmaskSection.size > 6
        ? fetchBuffer(`${apiHost}/api/grib/binary-chunk/${bitmaskOffset}/${bitmaskLength}/${fileName}`).then(b=>[b])
        : Promise.resolve([])

    const binaryOffset = binarySection.offset + 5
    const binaryLength = binarySection.size - 5
    let fetchPromise: Promise<[GribMessage[], ArrayBuffer[], ArrayBuffer[]]> = Promise.all([
            Promise.resolve([grib]),
            fetchBuffer(`${apiHost}/api/grib/binary-chunk/${binaryOffset}/${binaryLength}/${fileName}`).then(b=>[b]),
            bitmaskPromise,
        ])

    if(isCalculatedWindDirection(grib)) fetchPromise = fetchWindData(grib, gribList)
    if(isCalculatedHourPrecipitation(grib)) fetchPromise = fetchHourPrecipitationData(grib, gribList)
    
    return fetchPromise
        .then(([messages, buffers, bitmasks]) => [
            messages,
            buffers.map(b => new Uint8Array(b)),
            bitmasks.map(b => new Uint8Array(b)),
        ])
}
