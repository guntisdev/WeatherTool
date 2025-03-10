import { Component } from 'solid-js'
import { apiHost } from '../consts'
import { fetchText } from '../helpers/fetch'

//  Eiropa_LTV_pilsetas_nakama_dn.csv
//  Eiropa_LTV_pilsetas_tekosa_dn.csv
//  Latvija_LTV_pilsetas_nakama_dnn.csv
//  Latvija_LTV_pilsetas_tekosa_dn.csv
//  Latvija_faktiskais_laiks.csv

export const LvgmcForecast: Component<{}> = () => {
    fetchText(`${apiHost}/api/show/lvgmc-forecast/Latvija_LTV_pilsetas_tekosa_dn.csv`)
        .then(console.log)

    return <>
        LvgmcForecast
    </>
}
