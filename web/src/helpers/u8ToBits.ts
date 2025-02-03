export type U8bits = [boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean]

export function u8ToBits(dec: number): U8bits {
    const str = (dec >>> 0).toString(2)
    const str8 = leftPad(str)
    const arr: U8bits = [false, false, false, false, false, false, false, false]
    for (let i=0; i<8; i++) {
        arr[i] = str8[i] === '1' ? true : false
    }
    return arr
}

function leftPad(str: string): string {
    const padCount = 8 - str.length
    for(let i=0; i<padCount; i++) {
        str = '0' + str
    }
    return str
}
