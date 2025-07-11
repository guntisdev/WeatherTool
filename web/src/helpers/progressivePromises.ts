async function processPromises<T>(
    promiseFns: (() => Promise<T>)[],
    onProgress: (result: T) => void,
): Promise<(T | undefined)[]> {
    const allPromises = promiseFns.map(async promise => {
        try {
            const result = await promise();
            onProgress(result)
            return result
        } catch (error) {
            return undefined
        }
    })

    const results = await Promise.allSettled(allPromises)
    return results.map(result => 
        result.status === 'fulfilled' ? result.value : undefined
    )
}

export async function processPromisesInBatches<T>(
    promiseFns: (() => Promise<T>)[],
    onProgress: (result: T) => void,
    batchSize = 10,
): Promise<(T | undefined)[]> {
    const results = []
    while(await promiseFns.length > 0) {
        const size = Math.min(batchSize, promiseFns.length)
        const batchPromises = promiseFns.splice(0, size)
        const batchResults = await processPromises(batchPromises, onProgress)
        results.push(...batchResults)
    }

    return results
}
