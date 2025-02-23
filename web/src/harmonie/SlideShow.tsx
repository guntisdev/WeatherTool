import { Accessor, Component } from 'solid-js'

import styles from './harmonie.module.css'

export const SlideShow: Component<{
    getCanvas: Accessor<HTMLCanvasElement | undefined>,
    getImgList: Accessor<[string, ImageBitmap | undefined][]>,
}> = ({
    getCanvas,
    getImgList
}) => {
    function draw(img: ImageBitmap | undefined) {
        const canvas = getCanvas()!
        const ctx = canvas.getContext('2d')!
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        if (!img) return;

        ctx.drawImage(img, 0, 0)
    }

    return <ul class={styles.slideShow}>
        { getImgList().map(([forecastDate, img]) =>
        <li class={img ? styles.withImg : ''} onClick={() => draw(img)}>
            { format(forecastDate) }
        </li>)}
    </ul>
}

function format(date: string) {
    return date.slice(11, 13)
    // return date
}