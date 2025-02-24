import { Accessor, Component, createSignal } from 'solid-js'

import styles from './harmonie.module.css'
import { downloadImagesAsZip } from '../helpers/download'

export const SlideShow: Component<{
    getIsLoading: Accessor<boolean>,
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    getImgList: Accessor<[string, ImageBitmap | undefined][]>,
    getRefDate: Accessor<string>,
}> = ({
    getIsLoading,
    getCanvas,
    getImgList,
    getRefDate,
}) => {
    const [getActive, setActive] = createSignal(-1)
    const [getIsPlaying, setIsPlaying] = createSignal(false)

    function draw(i: number) {
        const canvas = getCanvas()!
        const ctx = canvas.getContext('2d')!
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        setActive(i)
        const img = getImgList()[i][1]
        if (!img) return;

        ctx.drawImage(img, 0, 0)
    }

    function next(delta = 1) {
        const count = getImgList().length
        if (!count) return;

        const result = (getActive() + delta) % count
        const nextValue = result >= 0 ? result : count - 1
        setActive(nextValue)
        draw(nextValue)
    }

    function prev() { next(-1) }

    function areControlsVisible(): boolean {
        return getImgList().length > 0 && !getIsLoading()
    }

    let playingTimeout = 0
    function play() {
        if (getIsPlaying()) {
            clearTimeout(playingTimeout)
            setIsPlaying(false)
            return;
        }

        function loop() {
            next()
            playingTimeout = setTimeout(loop, 300)
        }
        setIsPlaying(true)
        loop()
    }

    function download() {
        const imgs = getImgList().filter(([,img]) => !!img) as [string, ImageBitmap][]
        downloadImagesAsZip(imgs, getRefDate())
    }

    return <>
        <div class={styles.slideShowControls} style={{visibility: areControlsVisible() ? 'visible' : 'hidden'}}>
            <div class={styles.leftButtons}>
                <input type='button' value='prev' onClick={prev} />
                <input type='button' value={getIsPlaying()?'pause':'play'} onClick={play} />
                <input type='button' value='next' onClick={() => next()} />
            </div>
            <input type='button' value='download .zip' onClick={download} />
        </div>
        <ul class={styles.slideShowList}>
            { getImgList().map(([forecastDate, img], i) =>
            <li
                class={`${img?styles.withImg:''} ${i===getActive()?styles.active:''}`}
                onClick={() => draw(i)}
            >
                { format(forecastDate) }
            </li>)}
        </ul>
    </>
}

function format(date: string) {
    return date.slice(11, 13) // 2025-02-23T1500Z -> 15
}