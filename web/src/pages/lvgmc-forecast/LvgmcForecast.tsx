import { Component } from 'solid-js'

import dayBgMap from '../../assets/map_3840x1440_wind.webp'
import nightBgMap from '../../assets/map_night_3840x1440_wind.webp'
import { CsvMapData } from './CsvMapData'

//  Eiropa_LTV_pilsetas_nakama_dn.csv
//  Eiropa_LTV_pilsetas_tekosa_dn.csv
//  Latvija_LTV_pilsetas_nakama_dnn.csv
//  Latvija_LTV_pilsetas_tekosa_dn.csv
//  Latvija_faktiskais_laiks.csv

export const LvgmcForecast: Component<{}> = () => {
    return <>
        <p>Night</p>
        <CsvMapData
            csvUrl='Latvija_LTV_pilsetas_nakama_dnn.csv'
            bgImgUrl={nightBgMap}
        />

        <p>Day</p>
        <CsvMapData
            csvUrl='Latvija_LTV_pilsetas_tekosa_dn.csv'
            bgImgUrl={dayBgMap}
        />
    </>
}
