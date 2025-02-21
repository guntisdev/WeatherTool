import { Accessor, Component, createSignal } from 'solid-js'

import styles from './harmonie.module.css'
import { GribMessage } from './interfaces'

export const ReferenceTimes: Component<{
    getFileList: Accessor<string[]>,
    getGribList: Accessor<GribMessage[]>,
    onClick: () => void,
}> = ({
    getFileList,
    getGribList,
    onClick,
}) => {
    const [getActiveDate, setActiveDate] = createSignal('')
    const dateList = () => {
        const datesStr = getFileList().map(f => f.replace('harmonie_', '').split('_')[0])
        return [...new Set(datesStr)]
    }

    function onActiveDate(date: string) {
        onClick()
        const newValue = getActiveDate() === date ? '' : date
        setActiveDate(newValue)
    }

    return <ul class={styles.dateList}>
        { dateList().map(dateStr =>
            <li>
                <div onClick={() => onActiveDate(dateStr)}>
                    { dateStr }
                </div>
                <div class={styles.controls} style={{ display: getActiveDate() === dateStr ? 'block' : 'none'}}>
                    {
                        getGribList().some(g => g.time.referenceTime === dateStr)
                        && <>TODO: download all forecasts</>
                    }
                </div>
            </li>)}
    </ul>
}