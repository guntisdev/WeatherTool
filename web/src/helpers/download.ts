import JSZip from 'jszip'

export async function downloadImagesAsZip(
    images: [string, ImageBitmap][],
    filename = 'images',
): Promise<void> {
    filename = filename + '.zip'
    const strBlobArr = await Promise.all(
        images.map(async ([bitmapName, bitmap]): Promise<[string, Blob]> => {
            const blob = await bitmapToBlob(bitmap)
            return [bitmapName+'.webp', blob]
        })
    )

    const content = await createZip(strBlobArr)

    download(content, filename)
}

function bitmapToBlob( bitmap: ImageBitmap): Promise<Blob> {
    const canvas = document.createElement('canvas')
    canvas.width = bitmap.width
    canvas.height = bitmap.height
    const ctx = canvas.getContext('2d')!
    ctx.drawImage(bitmap, 0, 0)
    
    return new Promise<Blob>((resolve, reject) => {
        canvas.toBlob(
            (blob) => {
                if (blob) resolve(blob)
                else reject(new Error(`Failed to convert blob to WebP`))
            },
            'image/webp',
            0.8
        )
    })
}

export async function createZip(blobs: [string, Blob][]) {
    const zip = new JSZip()

    blobs.forEach(([name, blob]) => {
        zip.file(name, blob)
    })

    return await zip.generateAsync({
        type: 'blob',
        compression: 'DEFLATE',
        compressionOptions: {
            level: 6
        }
    })
}

export function download(content: Blob, name: string) {
    const downloadUrl = URL.createObjectURL(content)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    URL.revokeObjectURL(downloadUrl)
}
