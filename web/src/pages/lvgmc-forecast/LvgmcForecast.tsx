import { Component, createEffect, createSignal, Setter } from 'solid-js'

import dayBgMap from '../../assets/map_3840x1440_wind.webp'
import nightBgMap from '../../assets/map_night_3840x1440_wind.webp'
import { CsvMapData } from './CsvMapData'
import { Table } from '../../components/table/Table'
import { apiHost } from '../../consts'
import { fetchText } from '../../helpers/fetch'

//  Eiropa_LTV_pilsetas_nakama_dn.csv
//  Eiropa_LTV_pilsetas_tekosa_dn.csv
//  Latvija_LTV_pilsetas_nakama_dnn.csv
//  Latvija_LTV_pilsetas_tekosa_dn.csv
//  Latvija_faktiskais_laiks.csv

export const LvgmcForecast: Component<{}> = () => {
    const [getEveningLines, setEveningLines] = createSignal<string[]>([])
    const [getMorningLines, setMorningLines] = createSignal<string[]>([])

    const urlsAndSignals: [string, Setter<string[]>][] = [
        ['Latvija_LTV_pilsetas_nakama_dnn.csv', setEveningLines],
        ['Latvija_LTV_pilsetas_tekosa_dn.csv', setMorningLines],
    ]
    urlsAndSignals.forEach(([csvUrl, setLines]) =>
        fetchText(`${apiHost}/api/show/lvgmc-forecast/${csvUrl}`)
            .then(csv => setLines(csv.split('\n')))
            .catch(console.warn)
    )

    createEffect(() => {
        console.log(getMorningLines())
    })
    

    return <>
        <p>Vakara ēters</p>
        <CsvMapData
            getCityLines={() => getEveningLines().slice(4, 22)}
            getWindLine={() => getEveningLines()[26]}
            bgImgUrl={nightBgMap}
        />
        <CsvMapData
            getCityLines={() => getEveningLines().slice(31, 49)}
            getWindLine={() => getEveningLines()[57]}
            bgImgUrl={dayBgMap}
        />
        <Table getCsvLines={() => getEveningLines()} />

        <p>Rīta ēters</p>
        <CsvMapData
            getCityLines={() => getMorningLines().slice(4, 22)}
            getWindLine={() => getMorningLines()[23]}
            bgImgUrl={dayBgMap}
        />
        <Table getCsvLines={() => getMorningLines()} />
    </>
}
