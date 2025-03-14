import { Component, createEffect, Signal } from 'solid-js'

import { LoadingSpinner } from '../../components/spinner/LoadingSpinner'
import { CROP_BOUNDS, DrawOptions, GribMessage } from './interfaces'
import { drawGrib } from './draw/drawGrib'

export const DrawView: Component<{
    isLoadingSignal: Signal<boolean>,
    options: DrawOptions;
    canvasSignal: Signal<HTMLCanvasElement | undefined>,
    cachedMessagesSignal: Signal<GribMessage[]>,
    cachedBuffersSignal: Signal<Uint8Array[]>,
    cachedBitmasksSignal: Signal<Uint8Array[]>,
}> = ({
    isLoadingSignal: [getIsLoading, setIsLoading],
    options,
    canvasSignal: [getCanvas, setCanvas],
    cachedMessagesSignal: [getCachedMessages],
    cachedBuffersSignal: [getCachedBuffers],
    cachedBitmasksSignal: [getCachedBitmasks],
}) => {
    createEffect(async () => {
        setIsLoading(true)
        const canvas = getCanvas()!
        const ctx = canvas.getContext('2d')!
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        const cropBounds = options.getIsCrop() ? CROP_BOUNDS : undefined
        const contour = options.getIsContour()
        const isInterpolated = options.getIsInterpolated()
        if (getCachedMessages().length === 0) return;
        await new Promise(resolve => setTimeout(resolve, 100))
        // hack to show loading spinner
        drawGrib(canvas, getCachedMessages(), getCachedBuffers(), getCachedBitmasks(), cropBounds, contour, isInterpolated)
        setIsLoading(false)
    })

    return <>
        { getIsLoading() && <LoadingSpinner text='' />}
        <canvas ref={setCanvas} style={{ display: getIsLoading() ? 'none' : 'block' }} />
    </>
}
