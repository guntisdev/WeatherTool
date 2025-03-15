import { Component, createSignal, Setter } from 'solid-js'

import dayBgMap from '../../assets/map_3840x1440_wind.webp'
import nightBgMap from '../../assets/map_night_3840x1440_wind.webp'
import { CsvMapData } from './CsvMapData'
import { TableFour } from '../../components/table/TableFour'
import { apiHost } from '../../consts'
import { fetchText } from '../../helpers/fetch'
import { TableTwo } from '../../components/table/TableTwo'
import styles from './lvgmc.module.css'

//  Eiropa_LTV_pilsetas_nakama_dn.csv
//  Eiropa_LTV_pilsetas_tekosa_dn.csv
//  Latvija_LTV_pilsetas_nakama_dnn.csv
//  Latvija_LTV_pilsetas_tekosa_dn.csv
//  Latvija_faktiskais_laiks.csv

type Section = 'evening' | 'morning'

export const LvgmcForecast: Component<{}> = () => {
    const [getActiveSection, setActiveSection] = createSignal<Section>('evening')
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

    return <>
        <input
            type='button'
            value='Vakara ēters'
            onClick={()=>setActiveSection('evening')}
            class={getActiveSection() === 'evening' ? styles.activeButton : styles.button}
        />
        <input
            type='button'
            value='Rīta ēters'
            onClick={()=>setActiveSection('morning')}
            class={getActiveSection() === 'morning' ? styles.activeButton : styles.button}
        />
        <br/><br/>
        
        <div style={{ display: getActiveSection() === 'evening' ? 'block' : 'none' }}>
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
            <TableFour getCsvLines={() => getEveningLines()} />
            <div class={styles.twoCanvas}>
                <TableTwo getCsvLines={() => getEveningLines().slice(60, 67)} />
                <TableTwo getCsvLines={() => getEveningLines().slice(69, 76)} />
            </div>
        </div>

        <div style={{ display: getActiveSection() === 'morning' ? 'block' : 'none' }}>
            <CsvMapData
                getCityLines={() => getMorningLines().slice(4, 22)}
                getWindLine={() => getMorningLines()[23]}
                bgImgUrl={dayBgMap}
            />
            <TableFour getCsvLines={() => getMorningLines()} />
            <div class={styles.twoCanvas}>
                <TableTwo getCsvLines={() => getMorningLines().slice(33, 40)} />
                <TableTwo getCsvLines={() => getMorningLines().slice(42, 49)} />
            </div>
        </div>
    </>
}
