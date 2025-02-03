import { u8ToBits } from '../../helpers/u8ToBits.js'
import { MeteoGrid } from '../interfaces.js'

export function applyBitmask(
    grid: MeteoGrid,
    buffer: Uint8Array,
    bitmask: Uint8Array,
    bytesPerPoint: number,
): Uint8Array {
    const newBuffer = new Uint8Array(grid.rows * grid.cols * bytesPerPoint)

    let i=0, bufferI=0
    for (; i<bitmask.length; i++) {
        const bits = u8ToBits(bitmask[i])
        for (let bitI=0; bitI<bits.length; bitI++) {
            const newI = (i*8 + bitI) * bytesPerPoint
            if (newI >= newBuffer.length) {
                break;
            }
            if (bits[bitI]) {
                newBuffer[newI] = buffer[bufferI]
                newBuffer[newI+1] = buffer[bufferI+1]
                newBuffer[newI+2] = buffer[bufferI+2]
                bufferI += bytesPerPoint
            } else {
                newBuffer[newI] = 255
                newBuffer[newI+1] = 255
                newBuffer[newI+2] = 255
            }
        }
    }

    return newBuffer
}
